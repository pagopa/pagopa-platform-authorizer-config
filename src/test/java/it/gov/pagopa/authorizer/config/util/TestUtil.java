package it.gov.pagopa.authorizer.config.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.config.entity.AuthorizedEntity;
import it.gov.pagopa.authorizer.config.entity.GenericPair;
import it.gov.pagopa.authorizer.config.entity.Metadata;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.model.PageInfo;
import it.gov.pagopa.authorizer.config.model.authorization.*;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorization;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorizationList;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitution;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionList;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionStation;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionStationList;
import lombok.experimental.UtilityClass;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;

@UtilityClass
public class TestUtil {

    public <T> Page<T> mockPage(List<T> content, int limit, int pageNumber) {
        @SuppressWarnings("unchecked")
        Page<T> page = Mockito.mock(Page.class);
        when(page.getTotalPages()).thenReturn((int) Math.ceil((double) content.size() / limit));
        when(page.getNumberOfElements()).thenReturn(content.size());
        when(page.getNumber()).thenReturn(pageNumber);
        when(page.getSize()).thenReturn(limit);
        when(page.getContent()).thenReturn(content);
        when(page.stream()).thenReturn(content.stream());
        return page;
    }

    /**
     * @param object to map into the Json string
     * @return object as Json string
     * @throws JsonProcessingException if there is an error during the parsing of the object
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    /**
     * @param relativePath Path from source root of the json file
     * @return the Json string read from the file
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable
     *     byte sequence is read
     */
    public String readJsonFromFile(String relativePath) throws IOException {
        ClassLoader classLoader = TestUtil.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(relativePath)).getPath());
        return Files.readString(file.toPath());
    }

    public <T> T getObjectFromFile(Class<T> clazz, String relativePath) throws IOException {
        return new ObjectMapper().readValue(readJsonFromFile(relativePath), clazz);
    }
    public <T> T getObjectFromFile(TypeReference<T> clazz, String relativePath) throws IOException {
        return new ObjectMapper().readValue(readJsonFromFile(relativePath), clazz);
    }

    public AuthorizationList getAuthorizations(String domain, String ownerId) {
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

    public Authorization getAuthorization(int order, String id, String domain, String ownerId) {
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

    public CachedAuthorizationList getCachedAuthorizationList(String domain, String ownerId, boolean formatTTL) {
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

    public CachedAuthorization getCachedAuthorization(String description, String ownerId, String ttl) {
        return CachedAuthorization.builder()
                .description(description)
                .owner(ownerId)
                .subscriptionKey("sub-key-" + ownerId)
                .ttl(ttl)
                .build();
    }

    public EnrolledCreditorInstitutionList getEnrolledCreditorInstitutionList() {
        return EnrolledCreditorInstitutionList.builder()
                .creditorInstitutions(List.of(
                        EnrolledCreditorInstitution.builder()
                                .organizationFiscalCode("organization1")
                                .segregationCodes(List.of("01", "02"))
                                .build(),
                        EnrolledCreditorInstitution.builder()
                                .organizationFiscalCode("organization2")
                                .segregationCodes(List.of("03"))
                                .build()
                ))
                .build();
    }

    public EnrolledCreditorInstitutionStationList getEnrolledCreditorInstitutionStationList() {
        return EnrolledCreditorInstitutionStationList.builder()
                .stations(List.of(
                        EnrolledCreditorInstitutionStation.builder()
                                .stationId("station1")
                                .segregationCode("01")
                                .build(),
                        EnrolledCreditorInstitutionStation.builder()
                                .stationId("station2")
                                .segregationCode("02")
                                .build(),
                        EnrolledCreditorInstitutionStation.builder()
                                .stationId("station3")
                                .segregationCode("03")
                                .build()))
                .build();
    }

    public Page<SubscriptionKeyDomain> getSubscriptionKeyDomainsPaged(String domain, String ownerId) {
        return new PageImpl<>(getSubscriptionKeyDomains(domain, ownerId));
    }

    public List<SubscriptionKeyDomain> getSubscriptionKeyDomains(String domain, String ownerId) {
        return IntStream.rangeClosed(0, 5)
                .mapToObj(order -> getSubscriptionKeyDomain(order, "uuid-" + order, domain, ownerId))
                .collect(Collectors.toList());
    }

    public SubscriptionKeyDomain getSubscriptionKeyDomain(int order, String id, String domain, String ownerId) {
        return SubscriptionKeyDomain.builder()
                .id(id)
                .domain(domain)
                .subkey("subkey-" + order)
                .description("Authorization number " + order)
                .ownerId(Optional.ofNullable(ownerId).orElse("default-owner-id"))
                .ownerName("Owner-" + Optional.ofNullable(ownerId).orElse("default-owner-id"))
                .ownerType("CI")
                .authorizedEntities(List.of(getAuthorizedEntity(order)))
                .otherMetadata(List.of(
                        Metadata.builder()
                                .name("Appearance")
                                .shortKey("_app")
                                .content(List.of(
                                        GenericPair.builder()
                                                .key("color")
                                                .value("green")
                                                .build(),
                                        GenericPair.builder()
                                                .key("font")
                                                .value(String.valueOf(order))
                                                .build()))
                                .build(),
                        Metadata.builder()
                                .name("Language keywords")
                                .shortKey("_kwd")
                                .content(List.of(
                                        GenericPair.builder()
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

    private AuthorizedEntity getAuthorizedEntity(int order) {
        AuthorizedEntity result;
        if (order == 0) {
            result = AuthorizedEntity.builder()
                    .name("entity-name-all")
                    .value("*")
                    .build();
        } else if (order % 2 == 0) {
            result = AuthorizedEntity.builder()
                    .name("entity-name-" + order)
                    .value("entity" + order)
                    .build();
        } else {
            result = AuthorizedEntity.builder()
                    .name("entity-name-" + order)
                    .values(List.of("entity", "complex" + order))
                    .build();
        }
        return result;
    }
}
