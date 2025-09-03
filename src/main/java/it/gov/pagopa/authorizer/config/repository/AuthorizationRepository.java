package it.gov.pagopa.authorizer.config.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AuthorizationRepository extends CosmosRepository<SubscriptionKeyDomain, String> {

  @Query("SELECT * FROM skeydomains s WHERE s.domain = @domain AND s.subkey = @subscriptionKey")
  List<SubscriptionKeyDomain> findByDomainAndSubscriptionKey(@Param("domain") String domain, @Param("subscriptionKey") String subscriptionKey);

  @Query("SELECT * FROM skeydomains s WHERE s.domain = @domain AND (IS_NULL(@ownerId) OR s.ownerId = @ownerId)")
  List<SubscriptionKeyDomain> findByDomainAndOwnerId(@Param("domain") String domain, @Param("ownerId") String ownerId);

  List<SubscriptionKeyDomain> findByDomain(String domain);

  @Query("SELECT DISTINCT VALUE e[\"value\"] FROM s JOIN e IN s.authorizedEntities WHERE s.domain = @domain")
  Set<String> findAuthorizedEntitiesByDomain(String domain);

  Optional<SubscriptionKeyDomain> findBySubkey(String subkey);

  Page<SubscriptionKeyDomain> findByDomainAndOwnerId(String domain, String ownerId, Pageable pageable);

  Page<SubscriptionKeyDomain> findByDomain(String domain, Pageable pageable);
}
