package it.gov.pagopa.authorizer.config.service;

import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionList;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionStationList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.util.Constants;
import it.gov.pagopa.authorizer.config.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = Application.class)
class EnrolledOrganizationServiceTest {

    @MockBean private AuthorizationRepository authorizationRepository;

    @MockBean private WebClient apiconfigSelfcareClient;

    @Autowired @InjectMocks private EnrolledOrganizationService enrolledOrganizationService;

    @Test
    void getEnrolledOrganizations_200() throws IOException {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        when(authorizationRepository.findByDomain(domain)).thenReturn(TestUtil.getSubscriptionKeyDomains(domain, enrolledCI));
        mockWebClientCommunication("ok1");
        // executing logic
        EnrolledCreditorInstitutionList result = enrolledOrganizationService.getEnrolledOrganizations(domain);
        // executing assertion check
        assertNotNull(result);
        assertTrue(result.getCreditorInstitutions().size() > 0);
        assertFalse(result.getCreditorInstitutions()
                .stream()
                .anyMatch(creditorInstitution -> Constants.WILDCARD_CHARACTER.equals(creditorInstitution.getOrganizationFiscalCode())));
    }

    @Test
    void getEnrolledOrganizations_200_noElementFound() throws IOException {
        // initialize object
        String domain = "gpd";
        // Mocking objects
        when(authorizationRepository.findByDomain(domain)).thenReturn(List.of());
        mockWebClientCommunication("ok1");
        // executing logic
        EnrolledCreditorInstitutionList result = enrolledOrganizationService.getEnrolledOrganizations(domain);
        // executing assertion check
        assertNotNull(result);
        assertEquals(0, result.getCreditorInstitutions().size());
    }

    @Test
    void getEnrolledOrganizations_200_unparseableResponseFromAPIConfig() throws IOException {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        when(authorizationRepository.findByDomain(domain)).thenReturn(TestUtil.getSubscriptionKeyDomains(domain, enrolledCI));
        mockWebClientCommunication("ko1");
        // executing logic
        EnrolledCreditorInstitutionList result = enrolledOrganizationService.getEnrolledOrganizations(domain);
        // executing assertion check
        assertNotNull(result);
        assertEquals(0, result.getCreditorInstitutions().size());
    }

    @Test
    void getEnrolledOrganizations_200_nullResponseFromAPIConfig() throws IOException {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        when(authorizationRepository.findByDomain(domain)).thenReturn(TestUtil.getSubscriptionKeyDomains(domain, enrolledCI));
        mockWebClientCommunication(null);
        // executing logic
        EnrolledCreditorInstitutionList result = enrolledOrganizationService.getEnrolledOrganizations(domain);
        // executing assertion check
        assertNotNull(result);
        assertEquals(0, result.getCreditorInstitutions().size());
    }

    @Test
    void getEnrolledOrganizations_500_httpCommunicationError() {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        when(authorizationRepository.findByDomain(domain)).thenReturn(TestUtil.getSubscriptionKeyDomains(domain, enrolledCI));
        when(apiconfigSelfcareClient.get()).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        // executing logic and assertion check
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getEnrolledOrganizations(domain));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void getEnrolledOrganizations_500_invalidDomain() throws IOException {
        // initialize object
        String domain = "fake_domain";
        String enrolledCI = "77777777777";
        // Mocking objects
        when(authorizationRepository.findByDomain(domain)).thenReturn(TestUtil.getSubscriptionKeyDomains(domain, enrolledCI));
        when(apiconfigSelfcareClient.get()).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        // executing logic and assertion check
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getEnrolledOrganizations(domain));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void getStationsForEnrolledOrganizations_200() throws IOException {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        List<SubscriptionKeyDomain> subkeyDomains = TestUtil.getSubscriptionKeyDomains(domain, enrolledCI);
        subkeyDomains.get(2).getAuthorizedEntities().get(0).setValue(enrolledCI);
        when(authorizationRepository.findByDomain(domain)).thenReturn(subkeyDomains);
        mockWebClientCommunication("ok1");
        // executing logic
        EnrolledCreditorInstitutionStationList result = enrolledOrganizationService.getStationsForEnrolledOrganizations(enrolledCI, domain);
        // executing assertion check
        assertNotNull(result);
        assertTrue(result.getStations().size() > 0);
    }

    @Test
    void getStationsForEnrolledOrganizations_400_ciAsWildcard() {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "*";
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getStationsForEnrolledOrganizations(enrolledCI, domain));
        // executing assertion check
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void getStationsForEnrolledOrganizations_404_ciNotEnrolled() throws IOException {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        when(authorizationRepository.findByDomain(domain)).thenReturn(List.of());
        mockWebClientCommunication("ok1");
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getStationsForEnrolledOrganizations(enrolledCI, domain));
        // executing assertion check
        assertEquals(AppError.NOT_FOUND_CI_NOT_ENROLLED.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.NOT_FOUND_CI_NOT_ENROLLED.title, exception.getTitle());
    }

    @ParameterizedTest
    @CsvSource({
            "ok2",
            "ko1"
    })
    void getStationsForEnrolledOrganizations_404(String fileExtension) throws IOException {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        List<SubscriptionKeyDomain> subkeyDomains = TestUtil.getSubscriptionKeyDomains(domain, enrolledCI);
        subkeyDomains.get(2).getAuthorizedEntities().get(0).setValue(enrolledCI);
        when(authorizationRepository.findByDomain(domain)).thenReturn(subkeyDomains);
        mockWebClientCommunication(fileExtension);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getStationsForEnrolledOrganizations(enrolledCI, domain));
        // executing assertion check
        assertEquals(AppError.NOT_FOUND_NO_VALID_STATION.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.NOT_FOUND_NO_VALID_STATION.title, exception.getTitle());
    }

    @Test
    void getStationsForEnrolledOrganizations_404_nullResponseFromAPIConfig() throws IOException {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        List<SubscriptionKeyDomain> subkeyDomains = TestUtil.getSubscriptionKeyDomains(domain, enrolledCI);
        subkeyDomains.get(2).getAuthorizedEntities().get(0).setValue(enrolledCI);
        when(authorizationRepository.findByDomain(domain)).thenReturn(subkeyDomains);
        mockWebClientCommunication(null);
        // executing logic
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getStationsForEnrolledOrganizations(enrolledCI, domain));
        // executing assertion check
        assertEquals(AppError.NOT_FOUND_NO_VALID_STATION.httpStatus, exception.getHttpStatus());
        assertEquals(AppError.NOT_FOUND_NO_VALID_STATION.title, exception.getTitle());
    }

    @Test
    void getStationsForEnrolledOrganizations_500_httpCommunicationError() {
        // initialize object
        String domain = "gpd";
        String enrolledCI = "77777777777";
        // Mocking objects
        List<SubscriptionKeyDomain> subkeyDomains = TestUtil.getSubscriptionKeyDomains(domain, enrolledCI);
        subkeyDomains.get(2).getAuthorizedEntities().get(0).setValue(enrolledCI);
        when(authorizationRepository.findByDomain(domain)).thenReturn(subkeyDomains);
        when(apiconfigSelfcareClient.get()).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        // executing logic and assertion check
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getStationsForEnrolledOrganizations(enrolledCI, domain));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void getStationsForEnrolledOrganizations_500_invalidDomain() throws IOException {
        // initialize object
        String domain = "fake_domain";
        String enrolledCI = "77777777777";
        // Mocking objects
        List<SubscriptionKeyDomain> subkeyDomains = TestUtil.getSubscriptionKeyDomains(domain, enrolledCI);
        subkeyDomains.get(2).getAuthorizedEntities().get(0).setValue(enrolledCI);
        when(authorizationRepository.findByDomain(domain)).thenReturn(subkeyDomains);
        when(apiconfigSelfcareClient.get()).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        // executing logic and assertion check
        AppException exception = assertThrows(AppException.class, () -> enrolledOrganizationService.getStationsForEnrolledOrganizations(enrolledCI, domain));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }


    void mockWebClientCommunication(String status) throws IOException {
        String response = status != null ? TestUtil.readJsonFromFile(String.format("request/apiconfig/getsegregationcodes_%s.json", status)) : null;
        WebClient.RequestHeadersUriSpec uriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);
        Mono monoMock = Mockito.mock(Mono.class);

        when(apiconfigSelfcareClient.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(anyString(), anyString(), anyString())).thenReturn(headersSpecMock);
        when(headersSpecMock.accept(any(MediaType.class))).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.onStatus(notNull(), notNull())).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntity(any(Class.class))).thenReturn(monoMock);
        when(monoMock.block()).thenReturn(status != null ? ResponseEntity.of(Optional.ofNullable(response)) : null);
    }
}
