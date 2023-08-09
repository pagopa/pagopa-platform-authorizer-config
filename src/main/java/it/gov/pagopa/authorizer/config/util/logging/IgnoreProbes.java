package it.gov.pagopa.authorizer.config.util.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class IgnoreProbes extends Filter<ILoggingEvent> {

  private static final Pattern HEALTH_OR_PROMETHEUS = Pattern.compile("GET /actuator/health/");

  private Set<String> activeThreads = new HashSet<>();

  @Override
  public FilterReply decide(ILoggingEvent loggingEvent) {
    if (isHealthCheck(loggingEvent.getMessage())) {
      activeThreads.add(loggingEvent.getThreadName());
      return FilterReply.DENY;
    }  else {
      return FilterReply.ACCEPT;
    }
  }

  private boolean isHealthCheck(String message) {
    return HEALTH_OR_PROMETHEUS.matcher(message).matches();
  }
}
