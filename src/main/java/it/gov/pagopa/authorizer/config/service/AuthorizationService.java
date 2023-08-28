package it.gov.pagopa.authorizer.config.service;

import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.authorization.Authorization;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationList;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorization;
import it.gov.pagopa.authorizer.config.model.cachedauthorization.CachedAuthorizationList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.repository.CachedAuthorizationRepository;
import it.gov.pagopa.authorizer.config.util.CommonUtil;
import it.gov.pagopa.authorizer.config.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
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

  public void deleteAuthorization(@NotNull String authorizationId) {
    SubscriptionKeyDomain existingSubscriptionKeyDomain = authorizationRepository.findById(authorizationId).orElseThrow(() -> new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION, authorizationId));
    String domain = existingSubscriptionKeyDomain.getDomain();
    String subscriptionKey = existingSubscriptionKeyDomain.getSubkey();
    // save and return the final object
    try {
      authorizationRepository.delete(existingSubscriptionKeyDomain);
      cachedAuthorizationRepository.remove(domain, subscriptionKey);
    } catch (DataAccessException e) {
      log.error("An error occurred while deleting the authorization.", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR_DELETE);
    }
  }

  public CachedAuthorizationList getCachedAuthorization(@NotNull String domain, String ownerId, boolean convertTTL) {
    List<CachedAuthorization> cachedAuthorizations = new LinkedList<>();
    List<SubscriptionKeyDomain> entities = authorizationRepository.findByDomainAndOwnerId(domain, ownerId);
    // insert info about STORE's locking variable
    Long storeVariableTTL = cachedAuthorizationRepository.getTTL(domain);
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
      Long ttl = cachedAuthorizationRepository.getTTL(domain, subscriptionKey);
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
    List<SubscriptionKeyDomain> entities = authorizationRepository.findByDomainAndOwnerId(domain, ownerId);
    String now = LocalDateTime.now().format(Constants.DATE_FORMATTER);
    for (SubscriptionKeyDomain entity : entities) {
      entity.setLastForcedRefresh(now);
    }
    try {
      authorizationRepository.saveAll(entities);
    } catch (DataAccessException e) {
      log.error("An error occurred while refreshing cached authorizations. ", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR_REFRESH);
    }
  }
}
