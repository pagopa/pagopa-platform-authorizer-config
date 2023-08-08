package it.gov.pagopa.authorizer.config.mapper;

import it.gov.pagopa.authorizer.config.entity.AuthorizedEntity;
import it.gov.pagopa.authorizer.config.entity.Metadata;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationDetail;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationEntity;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationGenericKeyValue;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationMetadata;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationOwner;
import it.gov.pagopa.authorizer.config.model.authorization.OwnerType;
import it.gov.pagopa.authorizer.config.util.Constants;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionKeyDomainToAuthorizationDetailConverter implements Converter<SubscriptionKeyDomain, AuthorizationDetail> {

  @Override
  public AuthorizationDetail convert(MappingContext<SubscriptionKeyDomain, AuthorizationDetail> context) {
    @Valid SubscriptionKeyDomain source = context.getSource();
    return AuthorizationDetail.builder()
        .id(source.getId())
        .domain(source.getDomain())
        .subscriptionKey(source.getSubscriptionKey())
        .owner(AuthorizationOwner.builder()
            .id(source.getOwnerId())
            .name(source.getOwnerName())
            .type(OwnerType.fromString(source.getOwnerType()))
            .build())
        .description(source.getDescription())
        .authorizedEntities(convertAuthorizationEntities(source.getAuthorizedEntities()))
        .otherMetadata(convertMetadata(source.getOtherMetadata()))
        .insertedAt(source.getInsertedAt())
        .lastForcedRefresh(source.getLastForcedRefresh())
        .lastUpdate(Instant.ofEpochMilli(Long.parseLong(source.getLastUpdate()) * 1000)
            .atZone(ZoneId.of("UTC"))
            .format(Constants.DATE_FORMATTER))
        .build();
  }

  private List<AuthorizationEntity> convertAuthorizationEntities(@NotNull List<AuthorizedEntity> authorizedEntities) {
    return authorizedEntities.stream()
        .map(authorizedEntity -> AuthorizationEntity.builder()
            .name(authorizedEntity.getName())
            .value(authorizedEntity.getValue())
            .values(authorizedEntity.getValues())
            .build())
        .collect(Collectors.toList());
  }

  private List<AuthorizationMetadata> convertMetadata(@NotNull List<Metadata> metadata) {
    return metadata.stream()
        .map(singleMetadata -> AuthorizationMetadata.builder()
            .name(singleMetadata.getName())
            .shortKey(singleMetadata.getShortKey())
            .content(singleMetadata.getContent().stream()
                .map(genericPair -> AuthorizationGenericKeyValue.builder()
                    .key(genericPair.getKey())
                    .value(genericPair.getValue())
                    .values(genericPair.getValues())
                    .build())
                .collect(Collectors.toList())
            )
            .build())
        .collect(Collectors.toList());
  }
}