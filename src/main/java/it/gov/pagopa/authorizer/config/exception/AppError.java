package it.gov.pagopa.authorizer.config.exception;

import it.gov.pagopa.authorizer.config.util.Constants;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum AppError {


  BAD_REQUEST_WILDCARD_ORG(HttpStatus.BAD_REQUEST, "Bad request", "It is not possible to use wildcard character as organization fiscal code."),

  BAD_REQUEST_CHANGED_DOMAIN_OR_SUBKEY(HttpStatus.BAD_REQUEST, "Bad request", "It is not possible to change domain or subscription key in update."),

  BAD_REQUEST_INVALID_DOMAIN(HttpStatus.BAD_REQUEST, "Bad Request", "No valid service mapping for domain %s."),

  NOT_FOUND_NO_VALID_AUTHORIZATION(HttpStatus.NOT_FOUND, "Authorization not found", "No authorization with id [%s] was found."),

  NOT_FOUND_NO_VALID_AUTHORIZATION_WITH_SUBKEY(HttpStatus.NOT_FOUND, "Authorization not found", "No authorization with subkey [%s] was found."),

  NOT_FOUND_CI_NOT_ENROLLED(HttpStatus.NOT_FOUND, "Invalid creditor institution", "No creditor institution with fiscal code [%s] is enrolled to domain [%s]."),

  NOT_FOUND_NO_VALID_STATION(HttpStatus.NOT_FOUND, "Invalid station", "No creditor institution with fiscal code [%s] has valid registered stations for domain [%s]."),

  CONFLICT_AUTHORIZATION_ALREADY_EXISTENT(HttpStatus.CONFLICT, "Authorization already existent", "An existent authorization was found with domain [%s] and subscription key [%s]."),

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, Constants.INTERNAL_SERVER_ERROR, "Something went wrong during elaboration."),

  INTERNAL_SERVER_ERROR_CREATE(HttpStatus.INTERNAL_SERVER_ERROR, Constants.INTERNAL_SERVER_ERROR, "An error occurred while persisting the authorization."),

  INTERNAL_SERVER_ERROR_UPDATE(HttpStatus.INTERNAL_SERVER_ERROR, Constants.INTERNAL_SERVER_ERROR, "An error occurred while updating the authorization."),

  INTERNAL_SERVER_ERROR_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, Constants.INTERNAL_SERVER_ERROR, "An error occurred while deleting the authorization."),

  INTERNAL_SERVER_ERROR_REFRESH(HttpStatus.INTERNAL_SERVER_ERROR, Constants.INTERNAL_SERVER_ERROR, "An error occurred while refreshing the cached authorizations."),

  INTERNAL_SERVER_ERROR_MULTIPLE_AUTHORIZATION_WITH_SAME_SUBKEY(HttpStatus.INTERNAL_SERVER_ERROR, Constants.INTERNAL_SERVER_ERROR, "There are multiple authorization with the same subscription key [%s]. Please, check if they are correct.");

  public final HttpStatus httpStatus;
  public final String title;
  public final String details;


  AppError(HttpStatus httpStatus, String title, String details) {
    this.httpStatus = httpStatus;
    this.title = title;
    this.details = details;
  }
}


