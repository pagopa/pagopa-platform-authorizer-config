package it.gov.pagopa.authorizer.config.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

  public Object read(String key) {
    return template.opsForValue().get(key);
  }

  public void save(String key, Object value, long ttlInSeconds) {
    template.opsForValue().set(key, value, Duration.of(ttlInSeconds, TimeUnit.SECONDS.toChronoUnit()));
  }

  public boolean saveIfAbsent(String key, Object value, long ttlInSeconds) {
    return template.opsForValue().setIfAbsent(key, value, Duration.of(ttlInSeconds, TimeUnit.SECONDS.toChronoUnit()));
  }

  public void remove(String key) {
    template.delete(key);
  }

  public Long getTTL(String domain, String customKeyFormat) {
    return template.getExpire(getAPIMStoreVarKey(domain, customKeyFormat), TimeUnit.SECONDS);
  }

  public Long getTTL(String domain, String subscriptionKey, String customKeyFormat) {
    return template.getExpire(getAPIMKey(domain, subscriptionKey, customKeyFormat), TimeUnit.SECONDS);
  }

  public void removeSubscriptionKey(String domain, String subscriptionKey, String customKeyFormat) {
    template.delete(getAPIMKey(domain, subscriptionKey, customKeyFormat));
  }

  private String getAPIMStoreVarKey(String domain, String customKeyFormat) {
    return customKeyFormat != null ? String.format("%s_authorizer_%s", customKeyFormat, domain) : String.format(lockRefreshAuthorizationFormat, domain);
  }

  private String getAPIMKey(String domain, String subscriptionKey, String customKeyFormat) {
    return customKeyFormat != null ? String.format("%s_authorizer_%s_%s", customKeyFormat, domain, subscriptionKey) : String.format(cachedAuthorizationFormat, domain, subscriptionKey);
  }
}
