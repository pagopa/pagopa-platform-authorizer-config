package it.gov.pagopa.authorizer.config.util.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class ActuatorFilter extends AbstractMatcherFilter<ILoggingEvent> {

  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }

    if (event.getMessage().contains("actuator/health")) {
      return onMatch;
    } else {
      return onMismatch;
    }
  }
}

