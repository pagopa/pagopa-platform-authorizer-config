package it.gov.pagopa.authorizer.config.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.organization.CIAssociatedCode;
import it.gov.pagopa.authorizer.config.model.organization.CIAssociatedCodeList;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitution;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionStation;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionStationList;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.util.CommonUtil;
import it.gov.pagopa.authorizer.config.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.LinkedList;
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

  public EnrolledCreditorInstitutionList getEnrolledOrganizations(@NotNull String domain) {
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
          .map(enrolledCI -> getSegregationCodesFromEnrolledCI(enrolledCI, domain))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } catch (HttpClientErrorException e) {
      log.error("Error during communication with APIConfig for segregation codes retrieving. ", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR, "Communication error", "Error during communication with APIConfig for segregation codes retrieving.");
    }
    return EnrolledCreditorInstitutionList.builder()
        .creditorInstitutions(enrolledCreditorInstitutions)
        .build();
  }

  public EnrolledCreditorInstitutionStationList getStationsForEnrolledOrganizations(@NotNull String organizationFiscalCode, @NotNull String domain) {
    if (Constants.WILDCARD_CHARACTER.equals(organizationFiscalCode)) {
      throw new AppException(AppError.BAD_REQUEST_WILDCARD_ORG);
    }
    List<SubscriptionKeyDomain> subscriptionKeyDomains = authorizationRepository.findByDomain(domain);
    long count = subscriptionKeyDomains.stream()
        .map(subscriptionKeyDomain -> subscriptionKeyDomain.getAuthorizedEntities()
            .stream()
            .map(authorizedEntity -> authorizedEntity.getValues() != null ? StringUtils.join(authorizedEntity.getValues(), "|") : authorizedEntity.getValue())
            .filter(organizationFiscalCode::equals)
            .collect(Collectors.toList()))
        .mapToLong(List::size)
        .sum();
    boolean isCICorrectlyEnrolled = count != 0;
    log.info(String.format("Is CI with ID [%s] enrolled to domain [%s]? [%s].", organizationFiscalCode, domain, isCICorrectlyEnrolled));
    if (!isCICorrectlyEnrolled) {
      throw new AppException(AppError.NOT_FOUND_CI_NOT_ENROLLED, organizationFiscalCode, domain);
    }
    List<EnrolledCreditorInstitutionStation> enrolledCreditorInstitutionStations;
    try {
      enrolledCreditorInstitutionStations = getStationsFromEnrolledCI(organizationFiscalCode, domain);
    } catch (HttpClientErrorException e) {
      log.error("Error during communication with APIConfig for station retrieving. ", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR, "Communication error", "Error during communication with APIConfig for station retrieving.");
    }
    if (enrolledCreditorInstitutionStations.isEmpty()) {
      throw new AppException(AppError.NOT_FOUND_NO_VALID_STATION, organizationFiscalCode, domain);
    }
    return EnrolledCreditorInstitutionStationList.builder()
        .stations(enrolledCreditorInstitutionStations)
        .build();
  }

  private EnrolledCreditorInstitution getSegregationCodesFromEnrolledCI(String enrolledCI, String domain) {
    EnrolledCreditorInstitution result = null;
    CIAssociatedCodeList ciAssociatedCodes = executeCall(enrolledCI, CommonUtil.getServicePathFromDomain(domain));
    if (ciAssociatedCodes != null && !ciAssociatedCodes.getUsedCodes().isEmpty()) {
      List<CIAssociatedCode> usedCodes = ciAssociatedCodes.getUsedCodes();
      result = EnrolledCreditorInstitution.builder()
          .organizationFiscalCode(enrolledCI)
          .segregationCodes(usedCodes.stream()
              .map(CIAssociatedCode::getCode)
              .collect(Collectors.toList()))
          .build();
    }
    return result;
  }

  private List<EnrolledCreditorInstitutionStation> getStationsFromEnrolledCI(String enrolledCI, String domain) {
    List<EnrolledCreditorInstitutionStation> result = new LinkedList<>();
    CIAssociatedCodeList ciAssociatedCodes = executeCall(enrolledCI, CommonUtil.getServicePathFromDomain(domain));
    if (ciAssociatedCodes != null) {
      result = ciAssociatedCodes.getUsedCodes().stream()
          .map(ciAssociatedCode -> EnrolledCreditorInstitutionStation.builder()
              .segregationCode(ciAssociatedCode.getCode())
              .stationId(ciAssociatedCode.getStationName())
              .build())
          .collect(Collectors.toList());
    }
    return result;
  }

  private CIAssociatedCodeList executeCall(String enrolledCI, String service) {
    CIAssociatedCodeList result = null;
    log.info(String.format("Analyzing creditor institution with fiscal code [%s]: check if is enrolled to the domain.", enrolledCI));
    ResponseEntity<String> apiconfigResponse = executeCallToGetSegregationCodes(enrolledCI, service);
    // check if is retrieved a valid response and generate the list of codes
    try {
      if (HttpStatus.OK.equals(apiconfigResponse.getStatusCode()) && apiconfigResponse.getBody() != null) {
        result = jsonParser.readValue(apiconfigResponse.getBody(), CIAssociatedCodeList.class);
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
}
