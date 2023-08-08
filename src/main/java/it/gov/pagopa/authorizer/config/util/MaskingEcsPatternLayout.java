package it.gov.pagopa.authorizer.config.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MaskingEcsPatternLayout extends PatternLayout {

  private Pattern multilinePattern;
  private List<String> maskPatterns = new ArrayList<>();


  public void addMaskPattern(String maskPattern) {
    maskPatterns.add(maskPattern);
    multilinePattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE);
  }

  @Override
  public String doLayout(ILoggingEvent event) {
    return maskMessage(event.getFormattedMessage());
  }

  public String maskMessage(String message) {
    if (multilinePattern == null) {
      return message;
    }
    StringBuilder sb = new StringBuilder(message);
    Matcher matcher = multilinePattern.matcher(sb);
    while (matcher.find()) {
      IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
        if (matcher.group(group) != null) {
          IntStream.range(matcher.start(group), matcher.end(group)).forEach(i -> sb.setCharAt(i, '*'));
        }
      });
    }
    return sb.toString();
  }

}
