package it.gov.pagopa.authorizer.config.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.authorizer.config.model.ProblemJson;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionStationList;
import it.gov.pagopa.authorizer.config.model.organization.EnrolledCreditorInstitutionList;
import it.gov.pagopa.authorizer.config.service.EnrolledOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.constraints.NotBlank;

@RestController()
@RequestMapping(path = "/organizations")
@Tag(name = "Enrolled Orgs", description = "Everything about enrolled organizations")
@Validated
public class EnrolledOrganizationController {

  @Autowired
  private EnrolledOrganizationService enrolledOrganizationService;

  @Operation(
      summary = "Get list of organizations enrolled to a specific domain",
      security = { @SecurityRequirement(name = "ApiKey") },
      tags = { "Enrolled Orgs" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnrolledCreditorInstitutionList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @GetMapping(value = "/domains/{domain}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EnrolledCreditorInstitutionList> getEnrolledOrganizations(
      @Parameter(description = "The domain on which the organizations will be filtered.", required = true)
      @NotBlank @PathVariable("domain") String domain) {
    return ResponseEntity.ok(enrolledOrganizationService.getEnrolledOrganizations(domain));
  }

  @Operation(
      summary = "Get list of stations associated to organizations enrolled to a specific domain",
      security = { @SecurityRequirement(name = "ApiKey") },
      tags = { "Enrolled Orgs" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnrolledCreditorInstitutionList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @GetMapping(value = "{organizationfiscalcode}/domains/{domain}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EnrolledCreditorInstitutionStationList> getStationsForEnrolledOrganizations(
      @Parameter(description = "The enrolled organization on which the stations will be extracted.", required = true)
      @NotBlank @PathVariable("organizationfiscalcode") String organizationFiscalCode,
      @Parameter(description = "The domain on which the stations will be filtered.", required = true)
      @NotBlank @PathVariable("domain") String domain) {
    return ResponseEntity.ok(enrolledOrganizationService.getStationsForEnrolledOrganizations(organizationFiscalCode, domain));
  }
}
