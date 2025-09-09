package it.gov.pagopa.authorizer.config.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.repository.CachedAuthorizationRepository;
import it.gov.pagopa.authorizer.config.service.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AuthorizedEntitiesCacheScheduler {

    private AuthorizationService authorizationService;

    private CachedAuthorizationRepository cachedAuthorizationRepository;

    private String authorizedEntitiesDomainsLockKeyFormat;

    private List<String> authorizedEntitiesDomains;

    public AuthorizedEntitiesCacheScheduler(AuthorizationService authorizationService,
                                            CachedAuthorizationRepository cachedAuthorizationRepository,
                                            @Value("${authorizer.cache.authorized-entities.lock.key-format}") String authorizedEntitiesDomainsLockKeyFormat,
                                            @Value("${authorizer.schedule.authorized-entities.domains}") List<String> authorizedEntitiesDomains) {
        this.authorizationService = authorizationService;
        this.cachedAuthorizationRepository = cachedAuthorizationRepository;
        this.authorizedEntitiesDomainsLockKeyFormat = authorizedEntitiesDomainsLockKeyFormat;
        this.authorizedEntitiesDomains = authorizedEntitiesDomains;
    }

    @Scheduled(cron = "${authorizer.schedule.authorized-entities.expression}")
    public void autogenerateAuthorizedEntitiesCacheData() {
        for (String domain : authorizedEntitiesDomains) {
            if (lockForDomain(domain)) {
                try {
                    log.debug("Starting autogeneration the list of authorized entities for domain [{}]...", domain);
                    authorizationService.saveAuthorizedEntitiesInCache(domain);
                    log.debug("Autogeneration of the list of authorized entities for domain [{}] completed!", domain);
                } catch (Exception e) {
                    log.error("An error occurred while autogenerating the list of authorized entities by domain.", e);
                } finally {
                    unlockForDomain(domain);
                }
            }
        }
    }

    private boolean lockForDomain(String domain) {
        return cachedAuthorizationRepository.saveIfAbsent(String.format(authorizedEntitiesDomainsLockKeyFormat, domain), true, 60);
    }

    private void unlockForDomain(String domain) {
        cachedAuthorizationRepository.remove(String.format(authorizedEntitiesDomainsLockKeyFormat, domain));
    }
}
