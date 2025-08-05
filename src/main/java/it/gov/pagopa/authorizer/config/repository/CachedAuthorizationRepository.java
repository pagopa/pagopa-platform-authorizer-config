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

  @Value("${authorizer.cache.authorization.lock-key-format}")
  private String lockRefreshAuthorizationFormat;

  @Autowired
  @Qualifier("object")
  private RedisTemplate<String, Object> template;

  public Long getTTL(String domain, String customKeyFormat) {
    return template.getExpire(getAPIMStoreVarKey(domain, customKeyFormat), TimeUnit.SECONDS);
  }

  public Long getTTL(String domain, String subscriptionKey, String customKeyFormat) {
    return template.getExpire(getAPIMKey(domain, subscriptionKey, customKeyFormat), TimeUnit.SECONDS);
  }

  public void remove(String domain, String subscriptionKey, String customKeyFormat) {
    template.delete(getAPIMKey(domain, subscriptionKey, customKeyFormat));
  }

  private String getAPIMStoreVarKey(String domain, String customKeyFormat) {
    String key = customKeyFormat != null ? customKeyFormat : lockRefreshAuthorizationFormat;
    return String.format(key, domain);
  }

  private String getAPIMKey(String domain, String subscriptionKey, String customKeyFormat) {
    String key = customKeyFormat != null ? customKeyFormat : cachedAuthorizationFormat;
    return String.format(key, domain, subscriptionKey);
  }
}
