package it.gov.pagopa.authorizer.config.service;

import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.authorization.Authorization;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationList;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizedEntityList;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorizationList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.repository.CachedAuthorizationRepository;
import it.gov.pagopa.authorizer.config.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = Application.class)
class AuthorizationServiceTest {

    @MockBean private AuthorizationRepository authorizationRepository;

    @MockBean private CachedAuthorizationRepository cachedAuthorizationRepository;

    @Mock private Pageable pageable;

    @Mock private Page page;

    @Autowired @InjectMocks private AuthorizationService authorizationService;

    @ParameterizedTest
    @CsvSource({
            "gpd,",
            "gpd,77777777777"
    })
    void getAuthorizations_200(String domain, String ownerId) {
        // Mocking objects
        when(authorizationRepository.findByDomain(domain, pageable)).thenReturn(TestUtil.getSubscriptionKeyDomainsPaged(domain, "fakedomain"));
        when(authorizationRepository.findByDomainAndOwnerId(domain, ownerId, pageable)).thenReturn(TestUtil.getSubscriptionKeyDomainsPaged(domain, ownerId));
        // executing logic
        AuthorizationList result = authorizationService.getAuthorizations(domain, ownerId, pageable);
        // executing assertion check
        assertNotNull(result);
        assertTrue(result.getAuthorizations().size() > 0);
    }

    @ParameterizedTest
    @CsvSource({
            "gpd,",
            "gpd,77777777777"
    })
    void getAuthorizations_200_noResult(String domain, String ownerId) {
        // Mocking objects
        when(authorizationRepository.findByDomain(domain, pageable)).thenReturn(Page.empty());
        when(authorizationRepository.findByDomainAndOwnerId(domain, ownerId, pageable)).thenReturn(Page.empty());
        // executing logic
        AuthorizationList result = authorizationService.getAuthorizations(domain, ownerId, pageable);
        // executing assertion check
        assertNotNull(result);
        assertEquals(0, result.getAuthorizations().size());
    }

    @Test
    void getAuthorization_200() {
        // initialize objects
        String id = "fake_authorization_id";
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.ofNullable(TestUtil.getSubscriptionKeyDomain(1, id, "gpd", "fakedomain")));
        // executing logic
        Authorization result = authorizationService.getAuthorization(id);
        // executing assertion check
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertTrue(result.getAuthorizedEntities().size() > 0);
    }

    @Test
    void getAuthorization_404() {
        // initialize objects
        String id = "fake_authorization_id";
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.empty());
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.getAuthorization(id));
        // executing assertion check
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION.title, exception.getTitle());
    }

    @Test
    void getAuthorizationBySubscriptionKey_200() {
        // initialize objects
        String subkey = "fake_subkey";
        // Mocking objects
        SubscriptionKeyDomain subscriptionKeyDomain = TestUtil.getSubscriptionKeyDomain(1, subkey, "gpd", "fakedomain");
        subscriptionKeyDomain.setSubkey(subkey);
        when(authorizationRepository.findBySubkey(subkey)).thenReturn(Optional.ofNullable(subscriptionKeyDomain));
        // executing logic
        Authorization result = authorizationService.getAuthorizationBySubscriptionKey(subkey);
        // executing assertion check
        assertNotNull(result);
        assertEquals(subkey, result.getSubscriptionKey());
        assertTrue(result.getAuthorizedEntities().size() > 0);
    }

    @Test
    void getAuthorizationBySubscriptionKey_404() {
        // initialize objects
        String subkey = "fake_subkey";
        // Mocking objects
        when(authorizationRepository.findBySubkey(subkey)).thenReturn(Optional.empty());
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.getAuthorizationBySubscriptionKey(subkey));
        // executing assertion check
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION_WITH_SUBKEY.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION_WITH_SUBKEY.title, exception.getTitle());
    }

    @Test
    void getAuthorizationBySubscriptionKey_500() {
        // initialize objects
        String subkey = "fake_subkey";
        // Mocking objects
        when(authorizationRepository.findBySubkey(subkey)).thenThrow(CosmosAccessException.class);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.getAuthorizationBySubscriptionKey(subkey));
        // executing assertion check
        assertEquals(AppError.INTERNAL_SERVER_ERROR_MULTIPLE_AUTHORIZATION_WITH_SAME_SUBKEY.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.INTERNAL_SERVER_ERROR_MULTIPLE_AUTHORIZATION_WITH_SAME_SUBKEY.title, exception.getTitle());
    }

    @Test
    void createAuthorization_200() {
        // initialize objects
        String domain = "gpd";
        String subkey = "subkey-1";
        Authorization authorization = TestUtil.getAuthorization(1, "fake_id", domain, "77777777777");
        // Mocking objects
        when(authorizationRepository.findByDomainAndSubscriptionKey(domain, subkey)).thenReturn(List.of());
        when(authorizationRepository.save(any(SubscriptionKeyDomain.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        // executing logic
        Authorization result = authorizationService.createAuthorization(authorization);
        // executing assertion check
        assertNotNull(result);
        assertNotNull(result.getInsertedAt());
        assertNotNull(result.getLastForcedRefresh());
        assertNotNull(result.getLastUpdate());
        assertNotNull(result.getAuthorizedEntities());
        assertTrue(result.getAuthorizedEntities().size() > 0);
    }

    @Test
    void createAuthorization_409() {
        // initialize objects
        String domain = "gpd";
        String subkey = "subkey-1";
        Authorization authorization = TestUtil.getAuthorization(1, null, domain, "77777777777");
        // Mocking objects
        when(authorizationRepository.findByDomainAndSubscriptionKey(domain, subkey)).thenReturn(TestUtil.getSubscriptionKeyDomains(domain, "77777777777"));
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.createAuthorization(authorization));
        // executing assertion check
        assertEquals(AppError.CONFLICT_AUTHORIZATION_ALREADY_EXISTENT.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.CONFLICT_AUTHORIZATION_ALREADY_EXISTENT.title, exception.getTitle());
    }

    @Test
    void createAuthorization_500() {
        // initialize objects
        String domain = "gpd";
        String subkey = "subkey-1";
        Authorization authorization = TestUtil.getAuthorization(1, "fake_id", domain, "77777777777");
        // Mocking objects
        when(authorizationRepository.findByDomainAndSubscriptionKey(domain, subkey)).thenReturn(List.of());
        when(authorizationRepository.save(any(SubscriptionKeyDomain.class))).thenThrow(QueryTimeoutException.class);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.createAuthorization(authorization));
        // executing assertion check
        assertEquals(AppError.INTERNAL_SERVER_ERROR.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.INTERNAL_SERVER_ERROR.title, exception.getTitle());
    }

    @Test
    void updateAuthorization_200() {
        // initialize objects
        String domain = "gpd";
        String id = "fake_id";
        String ownerId = "77777777777";
        String lastForcedRefresh = "2023-06-01 11:30:00";
        String lastUpdate = "2023-06-01 11:30:00";
        Authorization authorization = TestUtil.getAuthorization(1, id, domain, ownerId);
        SubscriptionKeyDomain subkeyDomain = TestUtil.getSubscriptionKeyDomain(1, id, domain, ownerId);
        subkeyDomain.setLastForcedRefresh(lastForcedRefresh);
        subkeyDomain.setLastUpdate(lastUpdate);
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.of(subkeyDomain));
        when(authorizationRepository.save(any(SubscriptionKeyDomain.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        // executing logic
        Authorization result = authorizationService.updateAuthorization(id, authorization);
        // executing assertion check
        assertNotNull(result);
        assertEquals(result.getOwner().getId(), subkeyDomain.getOwnerId());
        assertEquals(result.getOwner().getName(), subkeyDomain.getOwnerName());
        assertEquals(result.getOwner().getType().getValue(), subkeyDomain.getOwnerType());
        assertEquals(result.getDescription(), subkeyDomain.getDescription());
        assertNotEquals(result.getLastForcedRefresh(), lastForcedRefresh);
        assertNotEquals(result.getLastUpdate(), lastUpdate);
        assertNotNull(result.getInsertedAt());
        assertNotNull(result.getAuthorizedEntities());
        assertEquals(authorization.getAuthorizedEntities().size(), result.getAuthorizedEntities().size());
    }

    @ParameterizedTest
    @CsvSource({
            "gpd,77777777777_fake",
            "gpd_fake,77777777777"
    })
    void updateAuthorization_400(String domain, String ownerId) {
        // initialize objects
        String id = "fake_id";
        Authorization authorization = TestUtil.getAuthorization(1, "gpd", "77777777777", ownerId);
        SubscriptionKeyDomain subkeyDomain = TestUtil.getSubscriptionKeyDomain(1, id, domain, ownerId);
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.of(subkeyDomain));
        when(authorizationRepository.save(any(SubscriptionKeyDomain.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.updateAuthorization(id, authorization));
        // executing assertion check
        assertEquals(AppError.BAD_REQUEST_CHANGED_DOMAIN_OR_SUBKEY.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.BAD_REQUEST_CHANGED_DOMAIN_OR_SUBKEY.title, exception.getTitle());
    }

    @Test
    void updateAuthorization_404() {
        // initialize objects
        String domain = "gpd";
        String id = "fake_id";
        String ownerId = "77777777777";
        Authorization authorization = TestUtil.getAuthorization(1, id, domain, ownerId);
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.empty());
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.updateAuthorization(id, authorization));
        // executing assertion check
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION.title, exception.getTitle());
    }

    @Test
    void updateAuthorization_500() {
        // initialize objects
        String domain = "gpd";
        String id = "fake_id";
        String ownerId = "77777777777";
        Authorization authorization = TestUtil.getAuthorization(1, id, domain, ownerId);
        SubscriptionKeyDomain subkeyDomain = TestUtil.getSubscriptionKeyDomain(1, id, domain, ownerId);
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.of(subkeyDomain));
        when(authorizationRepository.save(any(SubscriptionKeyDomain.class))).thenThrow(QueryTimeoutException.class);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.updateAuthorization(id, authorization));
        // executing assertion check
        assertEquals(AppError.INTERNAL_SERVER_ERROR.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.INTERNAL_SERVER_ERROR.title, exception.getTitle());
    }

    @Test
    void deleteAuthorization_200() {
        // initialize objects
        String id = "fake_authorization_id";
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.ofNullable(TestUtil.getSubscriptionKeyDomain(1, id, "gpd", "fakedomain")));
        doNothing().when(authorizationRepository).delete(any(SubscriptionKeyDomain.class));
        doNothing().when(cachedAuthorizationRepository).removeSubscriptionKey(anyString(), anyString(), anyString());
        // executing logic
        assertDoesNotThrow(() -> authorizationService.deleteAuthorization(id, null));
    }

    @Test
    void deleteAuthorization_404() {
        // initialize objects
        String id = "fake_authorization_id";
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.empty());
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.deleteAuthorization(id, null));
        // executing assertion check
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION.title, exception.getTitle());
    }

    @Test
    void deleteAuthorization_500() {
        // initialize objects
        String id = "fake_authorization_id";
        // Mocking objects
        when(authorizationRepository.findById(id)).thenReturn(Optional.ofNullable(TestUtil.getSubscriptionKeyDomain(1, id, "gpd", "fakedomain")));
        doThrow(QueryTimeoutException.class).when(authorizationRepository).delete(any(SubscriptionKeyDomain.class));
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.deleteAuthorization(id, null));
        // executing assertion check
        assertEquals(AppError.INTERNAL_SERVER_ERROR.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.INTERNAL_SERVER_ERROR.title, exception.getTitle());
    }

    @ParameterizedTest
    @CsvSource({
            "gpd,,true",
            "gpd,77777777777,true",
            "gpd,,false",
            "gpd,77777777777,false",
    })
    void getCachedAuthorization_200(String domain, String ownerId, boolean convertTTL) {
        // Mocking objects
        when(authorizationRepository.findByDomainAndOwnerId(domain, ownerId)).thenReturn(TestUtil.getSubscriptionKeyDomains(domain, ownerId));
        when(cachedAuthorizationRepository.getTTL(eq(domain), any())).thenReturn(1000L);
        when(cachedAuthorizationRepository.getTTL(anyString(), any(), any())).thenReturn(1200L);

        // executing logic
        CachedAuthorizationList result = authorizationService.getCachedAuthorization(domain, ownerId, convertTTL, null);
        // executing assertion check
        assertNotNull(result.getCachedAuthorizations());
        assertTrue(result.getCachedAuthorizations().size() > 0);
        assertNull(result.getCachedAuthorizations().get(0).getSubscriptionKey());
        assertNotNull(result.getCachedAuthorizations().get(1).getSubscriptionKey());
    }

    @ParameterizedTest
    @CsvSource({
            "gpd,,true",
            "gpd,77777777777,true",
            "gpd,,false",
            "gpd,77777777777,false",
    })
    void getCachedAuthorization_200_noAuthorization(String domain, String ownerId, boolean convertTTL) {
        // Mocking objects
        when(authorizationRepository.findByDomainAndOwnerId(domain, ownerId)).thenReturn(List.of());
        when(cachedAuthorizationRepository.getTTL(domain, null)).thenReturn(1000L);
        // executing logic
        CachedAuthorizationList result = authorizationService.getCachedAuthorization(domain, ownerId, convertTTL, null);
        // executing assertion check
        assertNotNull(result.getCachedAuthorizations());
        assertEquals(1, result.getCachedAuthorizations().size());
        assertNull(result.getCachedAuthorizations().get(0).getSubscriptionKey());
    }

    @ParameterizedTest
    @CsvSource({
            "gpd,,true",
            "gpd,77777777777,true",
            "gpd,,false",
            "gpd,77777777777,false",
    })
    void getCachedAuthorization_200_expiredDomain(String domain, String ownerId, boolean convertTTL) {
        // Mocking objects
        when(authorizationRepository.findByDomainAndOwnerId(domain, ownerId)).thenReturn(List.of());
        when(cachedAuthorizationRepository.getTTL(domain, null)).thenReturn(null);
        // executing logic
        CachedAuthorizationList result = authorizationService.getCachedAuthorization(domain, ownerId, convertTTL, null);
        // executing assertion check
        assertNotNull(result.getCachedAuthorizations());
        assertEquals(1, result.getCachedAuthorizations().size());
        assertNull(result.getCachedAuthorizations().get(0).getSubscriptionKey());
        assertEquals("Expired", result.getCachedAuthorizations().get(0).getTtl());
    }

    @ParameterizedTest
    @CsvSource({
            "gpd,",
            "gpd,77777777777",
    })
    void refreshCachedAuthorizations_200(String domain, String ownerId) {
        // Mocking objects
        when(authorizationRepository.findByDomain(eq(domain), any(Pageable.class))).thenReturn(TestUtil.getSubscriptionKeyDomainsPaged(domain, null));
        when(authorizationRepository.findByDomainAndOwnerId(eq(domain), eq(ownerId), any(Pageable.class))).thenReturn(TestUtil.getSubscriptionKeyDomainsPaged(domain, ownerId));
        when(authorizationRepository.saveAll(anyIterable())).thenAnswer(invocation -> invocation.getArguments()[0]);
        // executing logic
        assertDoesNotThrow(() -> authorizationService.refreshCachedAuthorizations(domain, ownerId));
    }

    @ParameterizedTest
    @CsvSource({
            "gpd,",
            "gpd,77777777777",
    })
    void refreshCachedAuthorizations_500(String domain, String ownerId) {
        // Mocking objects
        when(authorizationRepository.findByDomain(eq(domain), any(Pageable.class))).thenReturn(TestUtil.getSubscriptionKeyDomainsPaged(domain, null));
        when(authorizationRepository.findByDomainAndOwnerId(eq(domain), eq(ownerId), any(Pageable.class))).thenReturn(TestUtil.getSubscriptionKeyDomainsPaged(domain, ownerId));
        when(authorizationRepository.saveAll(anyIterable())).thenThrow(QueryTimeoutException.class);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.refreshCachedAuthorizations(domain, ownerId));
        // executing assertion check
        assertEquals(AppError.INTERNAL_SERVER_ERROR.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.INTERNAL_SERVER_ERROR.title, exception.getTitle());
    }




    @ParameterizedTest
    @CsvSource({
            "small-domain,10",
            "medium-domain,1000",
            "big-domain,10000",
            "huge-domain,50000",
    })
    void getAuthorizedEntitiesByDomain_200_notPreviouslyCached(String domain, String rawSize) {
        // Mocking objects
        int size = Integer.parseInt(rawSize);
        Set<String> authorizedEntitiesIdentifiers = TestUtil.getAuthorizedEntitiesIdentifiers(size);
        authorizedEntitiesIdentifiers.add("*"); // force wildcard setting
        when(cachedAuthorizationRepository.read(domain)).thenReturn(Set.of());
        when(authorizationRepository.findAuthorizedEntitiesByDomain(domain)).thenReturn(authorizedEntitiesIdentifiers);
        // executing logic
        AuthorizedEntityList result = authorizationService.getAuthorizedEntitiesByDomain(domain);
        // executing assertion check
        assertNotNull(result);
        assertEquals((int) result.getSize(), size);
        assertEquals(domain, result.getDomain());
        assertEquals(result.getAuthorizedEntities().size(), size);
        assertNotNull(result.getCreatedAt());
        assertFalse(result.getAuthorizedEntities().contains("*"));
    }

    @ParameterizedTest
    @CsvSource({
            "small-domain,10",
            "medium-domain,1000",
            "big-domain,10000",
            "huge-domain,50000",
    })
    void getAuthorizedEntitiesByDomain_200_previouslyCached(String domain, String rawSize) throws JsonProcessingException {
        // Mocking objects
        int size = Integer.parseInt(rawSize);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String mockedCachedContent = objectMapper.writeValueAsString(TestUtil.getAuthorizedEntities(domain, size));
        when(cachedAuthorizationRepository.read(anyString())).thenReturn(mockedCachedContent);
        // executing logic
        AuthorizedEntityList result = authorizationService.getAuthorizedEntitiesByDomain(domain);
        // executing assertion check
        assertNotNull(result);
        assertEquals((int) result.getSize(), size);
        assertEquals(domain, result.getDomain());
        assertEquals(result.getAuthorizedEntities().size(), size);
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void getAuthorizedEntitiesByDomain_200_noData() throws JsonProcessingException {
        // Mocking objects
        String domain = "fake-domain";
        int size = 0;
        when(cachedAuthorizationRepository.read(domain)).thenReturn(Set.of());
        when(authorizationRepository.findAuthorizedEntitiesByDomain(domain)).thenReturn(new HashSet<>());
        // executing logic
        AuthorizedEntityList result = authorizationService.getAuthorizedEntitiesByDomain(domain);
        // executing assertion check
        assertNotNull(result);
        assertEquals(size, (int) result.getSize());
        assertEquals(domain, result.getDomain());
        assertEquals(size, result.getAuthorizedEntities().size());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void getAuthorizedEntitiesByDomain_500() throws JsonProcessingException {
        // Mocking objects
        String domain = "fake-domain";
        int size = 10;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String mockedCachedContent = objectMapper.writeValueAsString(TestUtil.getAuthorizedEntities(domain, size));
        mockedCachedContent = mockedCachedContent.substring(0, mockedCachedContent.length() - 5);
        when(cachedAuthorizationRepository.read(anyString())).thenReturn(mockedCachedContent);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> authorizationService.getAuthorizedEntitiesByDomain(domain));
        // executing assertion check
        assertEquals(AppError.INTERNAL_SERVER_ERROR_RETRIEVE_AUTHORIZED_ENTITY.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.INTERNAL_SERVER_ERROR_RETRIEVE_AUTHORIZED_ENTITY.title, exception.getTitle());
        verify(cachedAuthorizationRepository, never()).save(anyString(), any(), anyLong());
    }
}
