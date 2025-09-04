package it.gov.pagopa.authorizer.config.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizedEntityList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.repository.CachedAuthorizationRepository;
import it.gov.pagopa.authorizer.config.service.AuthorizationService;
import it.gov.pagopa.authorizer.config.util.TestUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = Application.class)
public class AuthorizedEntitiesCacheScheduleTest {

    @MockBean private AuthorizationRepository authorizationRepository;

    @MockBean private CachedAuthorizationRepository cachedAuthorizationRepository;

    @Autowired @InjectMocks private AuthorizedEntitiesCacheScheduler authorizedEntitiesCacheScheduler;

    @ParameterizedTest
    @CsvSource({
            "domain1,1000",
            "domain1;domain2,1000",
            "domain1;domain2;domain3,10000",
            "domain1;domain2;domain3;domain4;domain5,50000"
    })
    public void autogenerateAuthorizedEntitiesCacheData_ok(String rawDomains, String rawSize) {
        // Mocking objects
        List<String> domains = List.of(rawDomains.split(";"));
        int numberOfDomains = domains.size();
        int size = Integer.parseInt(rawSize);
        Set<String> authorizedEntitiesIdentifiers = TestUtil.getAuthorizedEntitiesIdentifiers(size);
        authorizedEntitiesIdentifiers.add("*"); // force wildcard setting
        ReflectionTestUtils.setField(authorizedEntitiesCacheScheduler, "authorizedEntitiesDomains", domains);
        when(cachedAuthorizationRepository.saveIfAbsent(anyString(), any(), anyLong())).thenReturn(true);
        when(authorizationRepository.findAuthorizedEntitiesByDomain(anyString())).thenReturn(authorizedEntitiesIdentifiers);
        // executing logic
        authorizedEntitiesCacheScheduler.autogenerateAuthorizedEntitiesCacheData();
        // executing assertion check
        verify(cachedAuthorizationRepository, times(numberOfDomains)).saveIfAbsent(anyString(), any(), anyLong());
        verify(authorizationRepository, times(numberOfDomains)).findAuthorizedEntitiesByDomain(anyString());
        verify(cachedAuthorizationRepository, times(numberOfDomains)).save(anyString(), any(), anyLong());
        verify(cachedAuthorizationRepository, times(numberOfDomains)).remove(anyString());
    }

    @Test
    public void autogenerateAuthorizedEntitiesCacheData_ok_noDomains() {
        // Mocking objects
        ReflectionTestUtils.setField(authorizedEntitiesCacheScheduler, "authorizedEntitiesDomains", List.of());
        // executing logic
        authorizedEntitiesCacheScheduler.autogenerateAuthorizedEntitiesCacheData();
        // executing assertion check
        verify(cachedAuthorizationRepository, times(0)).saveIfAbsent(anyString(), any(), anyLong());
        verify(authorizationRepository, times(0)).findAuthorizedEntitiesByDomain(anyString());
        verify(cachedAuthorizationRepository, times(0)).save(anyString(), any(), anyLong());
        verify(cachedAuthorizationRepository, times(0)).remove(anyString());
    }

    @Test
    @SneakyThrows
    public void autogenerateAuthorizedEntitiesCacheData_error() {
        // Mocking objects
        List<String> domains = List.of("domain1", "domain2");
        int numberOfDomains = domains.size();
        int size = 1000;
        Set<String> authorizedEntitiesIdentifiers = TestUtil.getAuthorizedEntitiesIdentifiers(size);
        authorizedEntitiesIdentifiers.add("*"); // force wildcard setting
        ReflectionTestUtils.setField(authorizedEntitiesCacheScheduler, "authorizedEntitiesDomains", domains);
        when(cachedAuthorizationRepository.saveIfAbsent(anyString(), any(), anyLong())).thenReturn(true);
        when(authorizationRepository.findAuthorizedEntitiesByDomain(anyString())).thenThrow(RuntimeException.class);
        // executing logic
        authorizedEntitiesCacheScheduler.autogenerateAuthorizedEntitiesCacheData();
        // executing assertion check
        verify(cachedAuthorizationRepository, times(numberOfDomains)).saveIfAbsent(anyString(), any(), anyLong());
        verify(authorizationRepository, times(numberOfDomains)).findAuthorizedEntitiesByDomain(anyString());
        verify(cachedAuthorizationRepository, times(0)).save(anyString(), any(), anyLong());
        verify(cachedAuthorizationRepository, times(numberOfDomains)).remove(anyString());
    }
}
