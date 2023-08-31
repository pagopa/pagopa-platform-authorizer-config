package it.gov.pagopa.authorizer.config.mapper;

import it.gov.pagopa.authorizer.config.entity.AuthorizedEntity;
import it.gov.pagopa.authorizer.config.entity.GenericPair;
import it.gov.pagopa.authorizer.config.entity.Metadata;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.model.authorization.Authorization;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationEntity;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationMetadata;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationOwner;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthorizationDetailToSubscriptionKeyDomainConverter implements Converter<Authorization, SubscriptionKeyDomain> {

  @Override
  public SubscriptionKeyDomain convert(MappingContext<Authorization, SubscriptionKeyDomain> context) {
    @Valid Authorization source = context.getSource();
    @Valid @NotNull AuthorizationOwner owner = source.getOwner();
    return SubscriptionKeyDomain.builder()
        .id(source.getId() != null ? source.getId() : UUID.randomUUID().toString())
        .domain(source.getDomain())
        .subkey(source.getSubscriptionKey())
        .ownerId(owner.getId())
        .ownerName(owner.getName())
        .ownerType(owner.getType().getValue())
        .description(source.getDescription())
        .authorizedEntities(convertAuthorizerEntities(source.getAuthorizedEntities()))
        .otherMetadata(convertOtherMetadata(source.getOtherMetadata()))
        .build();
  }

  private List<AuthorizedEntity> convertAuthorizerEntities(@NotNull List<AuthorizationEntity> authorizedEntities) {
    return authorizedEntities.stream()
        .map(authorizationEntity -> AuthorizedEntity.builder()
            .name(authorizationEntity.getName())
            .value(authorizationEntity.getValue())
            .values(authorizationEntity.getValues())
            .build())
        .collect(Collectors.toList());
  }

  private List<Metadata> convertOtherMetadata(@NotNull List<AuthorizationMetadata> otherMetadata) {
    return otherMetadata.stream()
        .map(authorizationMetadata -> Metadata.builder()
            .name(authorizationMetadata.getName())
            .shortKey(authorizationMetadata.getShortKey())
            .content(authorizationMetadata.getContent().stream()
                .map(authorizationGenericKeyValue -> GenericPair.builder()
                    .key(authorizationGenericKeyValue.getKey())
                    .value(authorizationGenericKeyValue.getValue())
                    .values(authorizationGenericKeyValue.getValues())
                    .build())
                .collect(Collectors.toList())
            )
            .build())
        .collect(Collectors.toList());
  }
}
