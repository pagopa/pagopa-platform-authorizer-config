package it.gov.pagopa.authorizer.config.util;

import lombok.experimental.UtilityClass;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class Constants {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static final String HEADER_REQUEST_ID = "X-Request-Id";
}
