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
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorizationList;
import it.gov.pagopa.authorizer.config.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.*;

@RestController()
@RequestMapping(path = "/cachedauthorizations")
@Tag(name = "Cached Authorizations", description = "Everything about cached authorizations")
@Validated
public class CachedAuthorizationController {

  @Autowired
  private AuthorizationService authorizationService;


  /**
   * GET / : Get cached authorizations
   *
   * @param domain  The domain on which the authorizations will be filtered.
   * @param ownerId The identifier of the authorizations' owner.
   * @return OK (status code 200) or Too many request (status code 429) or Service unavailable
   * (status code 500)
   */
  @Operation(
      summary = "Get cached authorizations",
      security = {
          @SecurityRequirement(name = "ApiKey"),
      },
      tags = {"Cached Authorizations"})
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CachedAuthorizationList.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @GetMapping(value = "", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<CachedAuthorizationList> getAuthorizations(
      @Parameter(description = "The domain on which the authorizations will be filtered.", required = true)
      @NotBlank @RequestParam("domain") String domain,
      @Parameter(description = "The identifier of the authorizations' owner.")
      @RequestParam(value = "ownerId", required = false) String ownerId,
      @Parameter(description = "The identifier of the authorizations' owner.")
      @RequestParam(value = "formatTTL", required = false, defaultValue = "true") Boolean formatTTL) {
    return ResponseEntity.ok(authorizationService.getCachedAuthorization(domain, ownerId, formatTTL == null || formatTTL));
  }


  /**
   * POST /{domain}/refresh : Refresh cached authorizations by domain
   *
   * @param domain  The domain on which the authorizations will be filtered.
   * @param ownerId The identifier of the authorizations' owner.
   * @return OK (status code 200) or Bad Request (status code 400) or Conflict (status code 409) or Too many request (status code 429) or Service unavailable (status code 500)
   */
  @Operation(
      summary = "Refresh cached authorizations by domain",
      security = {
          @SecurityRequirement(name = "ApiKey")
      },
      tags = { "Cached Authorizations" })
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema())),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
          @ApiResponse(responseCode = "500", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
      })
  @PostMapping(value = "/{domain}/refresh", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Authorization> refreshCachedAuthorizations(
      @Parameter(description = "The domain on which the authorizations will be filtered.", required = true)
      @NotBlank @PathVariable("domain") String domain,
      @Parameter(description = "The identifier of the authorizations' owner.")
      @RequestParam(value = "ownerId", required = false) String ownerId) {
    authorizationService.refreshCachedAuthorizations(domain, ownerId);
    return ResponseEntity.ok().build();
  }
}
