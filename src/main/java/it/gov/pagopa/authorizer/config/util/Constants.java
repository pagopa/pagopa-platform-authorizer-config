package it.gov.pagopa.authorizer.config.util;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Constants {

  private Constants() {}

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static final Map<String, String> DOMAIN_TO_SERVICE_URI_MAPPING = Map.ofEntries(
      // insert here other static mapping from domain to service URI
      Map.entry("gpd", "gpd-payments/api/v1")
  );

  public static final String HEADER_REQUEST_ID = "X-Request-Id";

  public static final String WILDCARD_CHARACTER = "*";

  public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
}
