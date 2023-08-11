package it.gov.pagopa.authorizer.config.util;

import it.gov.pagopa.authorizer.config.model.PageInfo;
import org.springframework.data.domain.Page;

public class CommonUtil {

  private CommonUtil() {}

  /**
   * @param page Page returned from the database
   * @return return the page info
   */
  public static <T> PageInfo buildPageInfo(Page<T> page) {
    return PageInfo.builder()
        .page(page.getNumber())
        .limit(page.getSize())
        .totalPages(page.getTotalPages())
        .itemsFound(page.getNumberOfElements())
        .build();
  }

  public static String convertTTLToString(Long ttl) {
    return new StringBuilder()
        .append(ttl / 3600).append("h ")
        .append((ttl / 60) % 60).append("m ")
        .append(ttl % 60).append("s")
        .toString();
  }
}
