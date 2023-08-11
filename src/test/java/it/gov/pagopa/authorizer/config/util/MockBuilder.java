package it.gov.pagopa.authorizer.config.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.config.model.PageInfo;
import it.gov.pagopa.authorizer.config.model.authorization.*;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorization;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorizationList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MockBuilder {

    private MockBuilder() {}

    public static String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    public static AuthorizationList getAuthorizations(String domain, String ownerId) {
        return AuthorizationList.builder()
                .authorizations(IntStream.rangeClosed(1, 5)
                        .mapToObj(order -> getAuthorization(order, "uuid-" + order, domain, ownerId))
                        .collect(Collectors.toList()))
                .pageInfo(PageInfo.builder()
                        .page(0)
                        .limit(50)
                        .totalPages(1)
                        .itemsFound(5)
                        .build())
                .build();
    }

    public static Authorization getAuthorization(int order, String id, String domain, String ownerId) {
        return Authorization.builder()
                .id(id)
                .domain(domain)
                .subscriptionKey("subkey-" + order)
                .description("Authorization number " + order)
                .owner(AuthorizationOwner.builder()
                        .id(Optional.ofNullable(ownerId).orElse("default-owner-id"))
                        .name("Owner-" + Optional.ofNullable(ownerId).orElse("default-owner-id"))
                        .type(OwnerType.CI)
                        .build())
                .authorizedEntities(List.of(
                        AuthorizationEntity.builder()
                                .name("entity-name-" + (order * 2))
                                .value("entity-" + (order * 2))
                                .build(),
                        AuthorizationEntity.builder()
                                .name("entity-name-" + (order * 2))
                                .values(List.of(
                                        "sub-entity-A" + (order * 2),
                                        "sub-entity-B" + (order * 2)))
                                .build()))
                .otherMetadata(List.of(
                        AuthorizationMetadata.builder()
                                .name("Appearance")
                                .shortKey("_app")
                                .content(List.of(
                                        AuthorizationGenericKeyValue.builder()
                                                .key("color")
                                                .value("green")
                                                .build(),
                                        AuthorizationGenericKeyValue.builder()
                                                .key("font")
                                                .value(String.valueOf(order))
                                                .build()))
                                .build(),
                        AuthorizationMetadata.builder()
                                .name("Language keywords")
                                .shortKey("_kwd")
                                .content(List.of(
                                        AuthorizationGenericKeyValue.builder()
                                                .key("some-new-futuristic-language")
                                                .values(List.of("MOVE" + order, "DIV" + order, "->"))
                                                .build()))
                                .build()
                ))
                .insertedAt("2023-01-01 12:00:00")
                .lastUpdate("2023-06-01 20:00:00")
                .lastForcedRefresh("2023-12-01 08:00:00")
                .build();
    }

    public static CachedAuthorizationList getCachedAuthorizationList(String domain, String ownerId, boolean formatTTL) {
        List<CachedAuthorization> cachedAuthorizations = new ArrayList<>();
        cachedAuthorizations.add(getCachedAuthorization(
                String.format("Locking state for domain %s (remaining time before unlocking automatic refresh)", domain),
                null,
                formatTTL ? CommonUtil.convertTTLToString((long) (10000L * Math.random())) : String.valueOf((long) (10000L * Math.random()))));
        cachedAuthorizations.addAll(IntStream.rangeClosed(1, 5)
                .mapToObj(order -> getCachedAuthorization(null,
                        Optional.ofNullable(ownerId).orElse("owner" + order),
                        formatTTL ? CommonUtil.convertTTLToString((long) (10000L * Math.random())) : String.valueOf((long) (10000L * Math.random()))))
                .collect(Collectors.toList()));
        return CachedAuthorizationList.builder()
                .cachedAuthorizations(cachedAuthorizations)
                .build();
    }

    public static CachedAuthorization getCachedAuthorization(String description, String ownerId, String ttl) {
        return CachedAuthorization.builder()
                .description(description)
                .owner(ownerId)
                .subscriptionKey("sub-key-" + ownerId)
                .ttl(ttl)
                .build();
    }
}
