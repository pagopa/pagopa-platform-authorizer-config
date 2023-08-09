package it.gov.pagopa.authorizer.config.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.organization.CIAssociatedCode;
import it.gov.pagopa.authorizer.config.model.organization.CIAssociatedCodeList;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitution;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionStations;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutions;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrolledOrganizationService {

  @Value("${client.apiconfig-selfcare.uri}")
  private String apiconfigSelfcareIntegrationURI;

  @Autowired
  private AuthorizationRepository authorizationRepository;

  @Autowired
  private ObjectMapper jsonParser;

  @Autowired
  @Qualifier("apiconfigSelfcareClient")
  private WebClient apiconfigSelfcareClient;

  public EnrolledCreditorInstitutions getEnrolledOrganizations(@NotNull String domain) {
    List<SubscriptionKeyDomain> subscriptionKeyDomains = authorizationRepository.findByDomain(domain);

    List<String> distinctEnrolledCIs = subscriptionKeyDomains.stream()
        .map(subscriptionKeyDomain -> subscriptionKeyDomain.getAuthorizedEntities()
            .stream()
            .map(authorizedEntity -> authorizedEntity.getValues() != null ? StringUtils.join(authorizedEntity.getValues(), "|") : authorizedEntity.getValue())
            .collect(Collectors.toList()))
        .flatMap(List::stream)
        .distinct()
        .filter(enrolledCI -> !Constants.WILDCARD_CHARACTER.equals(enrolledCI))
        .collect(Collectors.toList());

    log.info(String.format("Found [%d] creditor institutions enrolled to the domain [%s]", distinctEnrolledCIs.size(), domain));
    List<EnrolledCreditorInstitution> enrolledCreditorInstitutions;
    try {
      enrolledCreditorInstitutions = distinctEnrolledCIs.stream()
          .map(enrolledCI -> executeCall(enrolledCI, getServicePathFromDomain(domain)))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } catch (HttpClientErrorException e) {
      log.error("Error during communication with APIConfig for segregation codes retrieving. ", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR, "Communication error", "Error during communication with APIConfig for segregation codes retrieving.");
    }
    return EnrolledCreditorInstitutions.builder()
        .creditorInstitutions(enrolledCreditorInstitutions)
        .build();
  }

  public EnrolledCreditorInstitutionStations getStationsForEnrolledOrganizations(@NotNull String organizationFiscalCode, @NotNull String domain) {
    return null;
  }

  private EnrolledCreditorInstitution executeCall(String enrolledCI, String service) {
    EnrolledCreditorInstitution result = null;
    log.info(String.format("Analyzing creditor institution with fiscal code [%s]: check if is enrolled to the domain.", enrolledCI));
    ResponseEntity<String> apiconfigResponse = executeCallToGetSegregationCodes(enrolledCI, service);
    // check if is retrieved a valid response and generate the list of codes
    try {
      if (HttpStatus.OK.equals(apiconfigResponse.getStatusCode()) && apiconfigResponse.getBody() != null) {
        CIAssociatedCodeList ciAssociatedCodeList = jsonParser.readValue(
            apiconfigResponse.getBody(), CIAssociatedCodeList.class);
        List<CIAssociatedCode> usedCodes = ciAssociatedCodeList.getUsedCodes();
        if (!usedCodes.isEmpty()) {
          result = EnrolledCreditorInstitution.builder()
              .organizationFiscalCode(enrolledCI)
              .segregationCodes(
                  usedCodes.stream().map(CIAssociatedCode::getCode).collect(Collectors.toList()))
              .build();
        }
      }
    } catch (JsonProcessingException e) {
      log.error("Error while generating object JSON from string. Unparseable string: ", e);
    }
    return result;
  }

  private ResponseEntity<String> executeCallToGetSegregationCodes(String enrolledCI, String service) {
    ResponseEntity<String> apiconfigResponse = apiconfigSelfcareClient.get()
        .uri(this.apiconfigSelfcareIntegrationURI, enrolledCI, service)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status == HttpStatus.BAD_REQUEST, clientResponse -> Mono.empty())
        .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> Mono.empty())
        .onStatus(status -> status.value() >= 500, clientResponse -> Mono.error(new HttpClientErrorException(clientResponse.statusCode())))
        .toEntity(String.class)
        .block();
    if (apiconfigResponse == null) {
      apiconfigResponse = ResponseEntity.notFound().build();
    }
    log.debug(String.format("Communication with APIConfig toward endpoint [%s] with parameters [%s, %s], returned HTTP status code [%s] and body [%s]", this.apiconfigSelfcareIntegrationURI, enrolledCI, service, apiconfigResponse.getStatusCode(), apiconfigResponse.getBody()));
    return apiconfigResponse;
  }


  private String getServicePathFromDomain(String domain) {
    String serviceUrl = Constants.DOMAIN_TO_SERVICE_URI_MAPPING.get(domain);
    if (serviceUrl == null) {
      throw new IllegalArgumentException(String.format("No valid service mapping for domain %s", domain));
    }
    return serviceUrl;
  }

}
