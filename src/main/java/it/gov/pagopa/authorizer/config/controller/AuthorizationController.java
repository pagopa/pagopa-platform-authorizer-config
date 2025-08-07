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
import it.gov.pagopa.authorizer.config.model.authorization.Authorization;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationList;
import it.gov.pagopa.authorizer.config.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@RestController()
@RequestMapping(path = "/authorizations")
@Tag(name = "Authorizations", description = "Everything about authorizations")
@Validated
public class AuthorizationController {

  @Autowired
  private AuthorizationService authorizationService;


  /**
   * GET / : Get authorization list
   *
   * @param domain The domain on which the authorizations will be filtered.
   * @param ownerId The identifier of the authorizations' owner.
   * @param limit The number of elements to be included in the page.
   * @param page The index of the page, starting from 0.
   * @return OK (status code 200) or Too many request (status code 429) or Service unavailable (status code 500)
   */
  @Operation(
      summary = "Get authorization list",
      security = {
          @SecurityRequirement(name = "ApiKey")
      },
      tags = { "Authorizations" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorizationList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @GetMapping(value = "", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<AuthorizationList> getAuthorizations(
      @Parameter(description = "The domain on which the authorizations will be filtered.", required = true)
      @NotBlank @RequestParam("domain") String domain,
      @Parameter(description = "The identifier of the authorizations' owner.")
      @RequestParam(value = "ownerId", required = false) String ownerId,
      @Parameter(description = "The number of elements to be included in the page.", required = true)
      @Valid @RequestParam(required = false, defaultValue = "10") @Positive @Max(999) Integer limit,
      @Parameter(description = "The index of the page, starting from 0.", required = true)
      @Valid @Min(0) @RequestParam(required = false, defaultValue = "0") Integer page) {
    return ResponseEntity.ok(authorizationService.getAuthorizations(domain, ownerId, PageRequest.of(page, limit)));
  }

  /**
   * GET /{authorizationId} : Get authorization by identifier
   *
   * @param authorizationId The identifier of the stored authorization.
   * @return OK (status code 200) or Not Found (status code 404) or Too many request (status code 429) or Service unavailable (status code 500)
   */
  @Operation(
      summary = "Get authorization by identifier",
      security = {
          @SecurityRequirement(name = "ApiKey")
      },
      tags = { "Authorizations" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorizationList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @GetMapping(value = "/{authorizationId}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Authorization> getAuthorization(
      @Parameter(description = "The identifier of the stored authorization.", required = true)
      @PathVariable("authorizationId") String authorizationId) {
    return ResponseEntity.ok(authorizationService.getAuthorization(authorizationId));
  }

  /**
   * GET /{authorizationId} : Get authorization by subscription key
   *
   * @param subscriptionKey The subscription key related to the stored authorization.
   * @return OK (status code 200) or Not Found (status code 404) or Too many request (status code 429) or Service unavailable/error (status code 500)
   */
  @Operation(
      summary = "Get authorization by subscription key",
      security = {
          @SecurityRequirement(name = "ApiKey")
      },
      tags = { "Authorizations" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorizationList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @GetMapping(value = "/subkey/{subscriptionKey}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Authorization> getAuthorizationBySubscriptionKey(
      @Parameter(description = "The subscription key related to the stored authorization.", required = true)
      @PathVariable("subscriptionKey") String subscriptionKey) {
    return ResponseEntity.ok(authorizationService.getAuthorizationBySubscriptionKey(subscriptionKey));
  }

  /**
   * POST / : Create new authorization
   *
   * @param authorization The authorization content to be created.
   * @return OK (status code 200) or Bad Request (status code 400) or Conflict (status code 409) or Too many request (status code 429) or Service unavailable (status code 500)
   */
  @Operation(
      summary = "Create new authorization",
      security = {
          @SecurityRequirement(name = "ApiKey")
      },
      tags = { "Authorizations" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorizationList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @PostMapping(value = "", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Authorization> createAuthorization(@RequestBody @Valid @NotNull Authorization authorization) {
    return ResponseEntity.ok(authorizationService.createAuthorization(authorization));
  }

  /**
   * PUT /{authorizationId} : Update existing authorization
   *
   * @param authorizationId The identifier of the stored authorization.
   * @param authorization The authorization content to be updated.
   * @return OK (status code 200) or Bad Request (status code 400) Not Found (status code 404) or Too many request (status code 429) or Service unavailable (status code 500)
   */
  @Operation(
      summary = "Update existing authorization",
      security = {
          @SecurityRequirement(name = "ApiKey")
      },
      tags = { "Authorizations" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorizationList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @PutMapping(value = "/{authorizationId}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Authorization> updateAuthorization(
      @Parameter(description = "The identifier of the stored authorization.", required = true)
      @PathVariable("authorizationId") String authorizationId,
      @RequestBody @Valid @NotNull Authorization authorization
  ) {
    return ResponseEntity.ok(authorizationService.updateAuthorization(authorizationId, authorization));
  }

  /**
   * DELETE /{authorizationId} : Delete existing authorization
   *
   * @param authorizationId The identifier of the stored authorization.
   * @return OK (status code 200) or Not Found (status code 404) or Too many request (status code 429) or Service unavailable (status code 500)
   */
  @Operation(
      summary = "Delete existing authorization",
      security = {
          @SecurityRequirement(name = "ApiKey")
      },
      tags = { "Authorizations" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @DeleteMapping(value = "/{authorizationId}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Authorization> deleteAuthorization(
      @Parameter(description = "The identifier of the stored authorization.", required = true)
      @PathVariable("authorizationId") String authorizationId,
      @Parameter(description = "Custom key for cache used by APIM")
      @RequestParam(value = "customKeyFormat", required = false) String customKeyFormat) {
    authorizationService.deleteAuthorization(authorizationId, customKeyFormat);
    return ResponseEntity.noContent().build();
  }
}
