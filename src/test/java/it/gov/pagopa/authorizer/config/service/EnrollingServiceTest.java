package it.gov.pagopa.authorizer.config.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.util.Constants;
import it.gov.pagopa.authorizer.config.util.TestUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = Application.class)
class EnrollingServiceTest {

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

    void mockWebClientCommunication(String status) throws IOException {
        String response = TestUtil.readJsonFromFile(String.format("request/apiconfig/getsegregationcodes_%s.json", status));
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
        when(monoMock.block()).thenReturn(ResponseEntity.of(Optional.of(response)));
    }
}
