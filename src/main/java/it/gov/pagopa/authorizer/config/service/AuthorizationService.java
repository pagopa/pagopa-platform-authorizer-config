package it.gov.pagopa.authorizer.config.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.authorization.Authorization;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationList;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizedEntityList;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorization;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorizationList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.repository.CachedAuthorizationRepository;
import it.gov.pagopa.authorizer.config.util.CommonUtil;
import it.gov.pagopa.authorizer.config.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthorizationService {

  @Autowired
  private CachedAuthorizationRepository cachedAuthorizationRepository;

  @Autowired
  private AuthorizationRepository authorizationRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private ObjectMapper rawMapper;

  @Value("${authorizer.configuration.limit}")
  private Integer configurationLimit;

  @Value("${authorizer.configuration.offset}")
  private Integer configurationOffset;

  @Value("${authorizer.cache.authorized-entities.key-format}")
  private String authorizedEntitiesDomainsKeyFormat;

  @Value("${authorizer.cache.authorized-entities.ttl}")
  private Long authorizedEntitiesTTL;

  public AuthorizationList getAuthorizations(@NotBlank String domain, String ownerId, @NotNull Pageable pageable) {
    Page<SubscriptionKeyDomain> page;
    if (ownerId != null) {
      page = authorizationRepository.findByDomainAndOwnerId(domain, ownerId, pageable);
    } else {
      page = authorizationRepository.findByDomain(domain, pageable);
    }
    return AuthorizationList.builder()
        .authorizations(page.isEmpty() ? List.of() : page.getContent().stream()
            .map(entity -> modelMapper.map(entity, Authorization.class))
            .collect(Collectors.toList()))
        .pageInfo(CommonUtil.buildPageInfo(page))
        .build();
  }

  public Authorization getAuthorizationBySubscriptionKey(@NotBlank String subscriptionKey) {
    SubscriptionKeyDomain entity;
    try {
      entity = authorizationRepository.findBySubkey(subscriptionKey).orElseThrow(() -> new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION_WITH_SUBKEY, subscriptionKey));
    } catch (DataAccessException e) {
      log.error("Error while reading authorizations by subscription key.", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR_MULTIPLE_AUTHORIZATION_WITH_SAME_SUBKEY, subscriptionKey);
    }
    return modelMapper.map(entity, Authorization.class);
  }

  public Authorization getAuthorization(@NotNull String authorizationId) {
    SubscriptionKeyDomain entity = authorizationRepository.findById(authorizationId).orElseThrow(() -> new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION, authorizationId));
    return modelMapper.map(entity, Authorization.class);
  }

  public Authorization createAuthorization(@NotNull Authorization authorization) {
    // check if another authorization for the same pair domain-subkey already exists
    if (!authorizationRepository.findByDomainAndSubscriptionKey(authorization.getDomain(), authorization.getSubscriptionKey()).isEmpty()) {
      throw new AppException(AppError.CONFLICT_AUTHORIZATION_ALREADY_EXISTENT, authorization.getDomain(), authorization.getSubscriptionKey());
    }
    // mapping the entity to be saved and update the dates
    SubscriptionKeyDomain subscriptionKeyDomain = modelMapper.map(authorization, SubscriptionKeyDomain.class);
    String now = LocalDateTime.now().format(Constants.DATE_FORMATTER);
    subscriptionKeyDomain.setInsertedAt(now);
    subscriptionKeyDomain.setLastForcedRefresh(now);
    subscriptionKeyDomain.setLastUpdate(now);
    // save and return the final object
    try {
      subscriptionKeyDomain = authorizationRepository.save(subscriptionKeyDomain);
    } catch (DataAccessException e) {
      log.error("An error occurred while creating the authorization.", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR_CREATE);
    }
    return modelMapper.map(subscriptionKeyDomain, Authorization.class);
  }

  public Authorization updateAuthorization(@NotNull String authorizationId, @NotNull Authorization authorization) {
    // check if the authorization with the ID already exists
    SubscriptionKeyDomain existingSubscriptionKeyDomain = authorizationRepository.findById(authorizationId).orElseThrow(() -> new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION, authorizationId));
    if (!existingSubscriptionKeyDomain.getDomain().equals(authorization.getDomain()) || !existingSubscriptionKeyDomain.getSubkey().equals(authorization.getSubscriptionKey())) {
      throw new AppException(AppError.BAD_REQUEST_CHANGED_DOMAIN_OR_SUBKEY);
    }
    // mapping the entity to be saved and update the dates
    SubscriptionKeyDomain subscriptionKeyDomain = modelMapper.map(authorization, SubscriptionKeyDomain.class);
    existingSubscriptionKeyDomain.setOwnerId(subscriptionKeyDomain.getOwnerId());
    existingSubscriptionKeyDomain.setOwnerName(subscriptionKeyDomain.getOwnerName());
    existingSubscriptionKeyDomain.setOwnerType(subscriptionKeyDomain.getOwnerType());
    existingSubscriptionKeyDomain.setDescription(subscriptionKeyDomain.getDescription());
    existingSubscriptionKeyDomain.setAuthorizedEntities(subscriptionKeyDomain.getAuthorizedEntities());
    existingSubscriptionKeyDomain.setOtherMetadata(subscriptionKeyDomain.getOtherMetadata());
    subscriptionKeyDomain.setInsertedAt(existingSubscriptionKeyDomain.getInsertedAt());
    String now = LocalDateTime.now().format(Constants.DATE_FORMATTER);
    existingSubscriptionKeyDomain.setLastForcedRefresh(now);
    existingSubscriptionKeyDomain.setLastUpdate(now);
    // save and return the final object
    try {
      existingSubscriptionKeyDomain = authorizationRepository.save(existingSubscriptionKeyDomain);
    } catch (DataAccessException e) {
      log.error("An error occurred while updating the authorization.", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR_UPDATE);
    }
    return modelMapper.map(existingSubscriptionKeyDomain, Authorization.class);
  }

  public void deleteAuthorization(@NotNull String authorizationId, String customKeyFormat) {
    SubscriptionKeyDomain existingSubscriptionKeyDomain = authorizationRepository.findById(authorizationId).orElseThrow(() -> new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION, authorizationId));
    String domain = existingSubscriptionKeyDomain.getDomain();
    String subscriptionKey = existingSubscriptionKeyDomain.getSubkey();
    // save and return the final object
    try {
      authorizationRepository.delete(existingSubscriptionKeyDomain);
      cachedAuthorizationRepository.removeSubscriptionKey(domain, subscriptionKey, customKeyFormat);
    } catch (DataAccessException e) {
      log.error("An error occurred while deleting the authorization.", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR_DELETE);
    }
  }

  public AuthorizedEntityList getAuthorizedEntitiesByDomain(@NotNull String domain) {
    AuthorizedEntityList authorizedEntities = new AuthorizedEntityList();
    try {
      // Retrieve the cached value from Redis storage
      Object rawValue = cachedAuthorizationRepository.read(String.format(authorizedEntitiesDomainsKeyFormat, domain));
      if (rawValue != null) {
        // If the value exists, then map the retrieved string in the required object
        authorizedEntities = rawMapper.readValue(rawValue.toString(), AuthorizedEntityList.class);
      } else {
        // If the value doesn't exist, then create it!
        authorizedEntities = saveAuthorizedEntitiesInCache(domain);
      }
    } catch (JsonProcessingException e) {
      log.error("An error occurred while retrieving the list of authorized entities by domain.", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR_RETRIEVE_AUTHORIZED_ENTITY, domain);
    }
    return authorizedEntities;
  }

  public CachedAuthorizationList getCachedAuthorization(@NotNull String domain, String ownerId, boolean convertTTL, String customKeyFormat) {
    List<CachedAuthorization> cachedAuthorizations = new LinkedList<>();
    List<SubscriptionKeyDomain> entities = authorizationRepository.findByDomainAndOwnerId(domain, ownerId);
    // insert info about STORE's locking variable
    Long storeVariableTTL = cachedAuthorizationRepository.getTTL(domain, customKeyFormat);
    String storeVariableTTLAsString = "Expired";
    if (storeVariableTTL != null) {
      storeVariableTTLAsString = convertTTL ? CommonUtil.convertTTLToString(storeVariableTTL) : Long.toString(storeVariableTTL);
    }
    cachedAuthorizations.add(CachedAuthorization.builder()
        .description(String.format("Locking state for domain %s (remaining time before unlocking automatic refresh)", domain))
        .ttl(storeVariableTTLAsString)
        .build());
    // insert info about all cached authorizations
    for (SubscriptionKeyDomain entity : entities) {
      String subscriptionKey = entity.getSubkey();
      Long ttl = cachedAuthorizationRepository.getTTL(domain, subscriptionKey, customKeyFormat);
      if (ttl != null && ttl > 0) {
        cachedAuthorizations.add(CachedAuthorization.builder()
            .owner(entity.getOwnerId())
            .subscriptionKey(subscriptionKey)
            .ttl(convertTTL ? CommonUtil.convertTTLToString(ttl) : Long.toString(ttl))
            .build());
      }
    }
    return CachedAuthorizationList.builder()
        .cachedAuthorizations(cachedAuthorizations)
        .build();
  }

  public void refreshCachedAuthorizations(@NotNull String domain, String ownerId) {
    Pageable pageable = PageRequest.of(configurationOffset, configurationLimit);
    Page<SubscriptionKeyDomain> page;
    String now = LocalDateTime.now().format(Constants.DATE_FORMATTER);

    do {
      page = ownerId == null
              ? authorizationRepository.findByDomain(domain, pageable)
              : authorizationRepository.findByDomainAndOwnerId(domain, ownerId, pageable);

      List<SubscriptionKeyDomain> entities = page.getContent();
      for (SubscriptionKeyDomain entity : entities) {
        entity.setLastForcedRefresh(now);
      }

      try {
        authorizationRepository.saveAll(entities);
      } catch (DataAccessException e) {
        log.error("An error occurred while refreshing cached authorizations.", e);
        throw new AppException(AppError.INTERNAL_SERVER_ERROR_REFRESH);
      }

      pageable = page.nextPageable();
    } while (page.hasNext());
  }

  public AuthorizedEntityList saveAuthorizedEntitiesInCache(String domain) throws JsonProcessingException {
    // Search in DB the aggregated list of authorized entities
    AuthorizedEntityList authorizedEntities = new AuthorizedEntityList();
    Set<String> authorizedEntitiesByDomain = authorizationRepository.findAuthorizedEntitiesByDomain(domain);
    // Map the retrieved set of authorized entities removing the unneeded values
    authorizedEntitiesByDomain.remove("*");
    authorizedEntities.setAuthorizedEntities(authorizedEntitiesByDomain);
    authorizedEntities.setSize(authorizedEntitiesByDomain.size());
    authorizedEntities.setDomain(domain);
    authorizedEntities.setCreatedAt(LocalDateTime.now());
    // If something is found, store the list of authorized entities in Redis storage
    if (!authorizedEntitiesByDomain.isEmpty()) {
      cachedAuthorizationRepository.save(
              String.format(authorizedEntitiesDomainsKeyFormat, domain),
              rawMapper.writeValueAsString(authorizedEntities),
              authorizedEntitiesTTL);
    }
    return authorizedEntities;
  }
}
