package it.gov.pagopa.authorizer.config.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CachedAuthorizationRepository {

  @Value("${authorizer.cache.authorization.key-format}")
  private String cachedAuthorizationFormat;

  @Autowired
  @Qualifier("object")
  private RedisTemplate<String, Object> template;

  public Long getTTL(String domain, String subscriptionKey) {
    return template.getExpire(getAPIMKey(domain, subscriptionKey), TimeUnit.SECONDS);
  }

  public void remove(String domain, String subscriptionKey) {
    template.delete(getAPIMKey(domain, subscriptionKey));
  }

  private String getAPIMKey(String domain, String subscriptionKey) {
    return String.format(cachedAuthorizationFormat, domain, subscriptionKey);
  }
}
