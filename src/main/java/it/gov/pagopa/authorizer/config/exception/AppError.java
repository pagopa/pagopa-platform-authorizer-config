package it.gov.pagopa.authorizer.config.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum AppError {

  AUTHORIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Authorization not found", "No authorization with id [%s] was found."),

  BAD_REQUEST_CHANGED_DOMAIN_OR_SUBKEY(HttpStatus.BAD_REQUEST, "Bad request", "It is not possible to change domain or subscription key in update."),

  AUTHORIZATION_CONFLICT(HttpStatus.CONFLICT, "Authorization already existent", "An existent authorization was found with domain [%s] and subscription key [%s]."),


  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something went wrong during elaboration.");

  public final HttpStatus httpStatus;
  public final String title;
  public final String details;


  AppError(HttpStatus httpStatus, String title, String details) {
    this.httpStatus = httpStatus;
    this.title = title;
    this.details = details;
  }
}


