package it.gov.pagopa.authorizer.config.model.authorization;

import it.gov.pagopa.authorizer.config.exception.AppException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.Arrays;

/**
 * The enumeration that define the possible values that can be associated to an authorization's owner.
 */
@Getter
public enum OwnerType {

  /**
   * Enumeration value for Broker.
   */
  BROKER("BROKER"),

  /**
   * Enumeration value for Creditor institution.
   */
  CI("CI"),

  /**
   * Enumeration value for generic kind of owner.
   */
  OTHER("OTHER"),

  /**
   * Enumeration value for Payment Service Provider.
   */
  PSP("PSP");

  /**
   * The string form of the enumeration.
   */
  private final String value;

  /**
   * Default constructor.
   *
   * @param value the content value.
   */
  OwnerType(final String value) {
    this.value = value;
  }

  /**
   * Get an enumeration value from the string form. If the passed string is not related to a valid
   * value, the method throws an {@link it.gov.pagopa.authorizer.config.exception.AppException}.
   *
   * @param value the string value to be converted.
   * @throws AppException if no enumeration value can be extracted from string
   * @return the enumeration value.
   */
  public static OwnerType fromString(String value) {
    return Arrays.stream(OwnerType.values())
        .filter(element -> element.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid owner type", "The string '" + value + "' cannot refers to a valid owner type."));
  }

}
