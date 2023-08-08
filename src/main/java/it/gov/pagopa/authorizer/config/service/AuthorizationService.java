package it.gov.pagopa.authorizer.config.service;

import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationDetail;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationDetailList;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthorizationService {

  @Autowired
  private AuthorizationRepository authorizationRepository;

  @Autowired
  private ModelMapper modelMapper;

  public AuthorizationDetailList getAuthorizations(@NotBlank String domain, String ownerId, @NotNull Pageable pageable) {
    Page<SubscriptionKeyDomain> resultSet;
    if (ownerId != null) {
      resultSet = authorizationRepository.findByDomainAndOwnerId(domain, ownerId, pageable);
    } else {
      resultSet = authorizationRepository.findByDomain(domain, pageable);
    }
    return AuthorizationDetailList.builder()
        .authorizations(resultSet.isEmpty() ? List.of() : resultSet.getContent().stream()
            .map(entity -> modelMapper.map(entity, AuthorizationDetail.class))
            .collect(Collectors.toList()))
        .build();
  }

  public AuthorizationDetail getAuthorization(@NotNull String authorizationId) {
    SubscriptionKeyDomain entity = authorizationRepository.findById(authorizationId).orElseThrow(() -> new AppException(AppError.AUTHORIZATION_NOT_FOUND, authorizationId));
    return modelMapper.map(entity, AuthorizationDetail.class);
  }

  public AuthorizationDetail createAuthorization(@NotNull AuthorizationDetail authorization) {
    // check if another authorization for the same pair domain-subkey already exists
    if (!authorizationRepository.findByDomainAndSubscriptionKey(authorization.getDomain(), authorization.getSubscriptionKey()).isEmpty()) {
      throw new AppException(AppError.AUTHORIZATION_CONFLICT, authorization.getDomain(), authorization.getSubscriptionKey());
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
      log.error("An error occurred while persisting the authorization.", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR, "Internal server error", "An error occurred while persisting the authorization.");
    }
    return modelMapper.map(subscriptionKeyDomain, AuthorizationDetail.class);
  }

  public AuthorizationDetail updateAuthorization(@NotNull String authorizationId, @NotNull AuthorizationDetail authorization) {
    // check if the authorization with the ID already exists
    SubscriptionKeyDomain existingSubscriptionKeyDomain = authorizationRepository.findById(authorizationId).orElseThrow(() -> new AppException(AppError.AUTHORIZATION_NOT_FOUND, authorizationId));
    if (!existingSubscriptionKeyDomain.getDomain().equals(authorization.getDomain()) || !existingSubscriptionKeyDomain.getSubscriptionKey().equals(authorization.getSubscriptionKey())) {
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
    return modelMapper.map(authorizationRepository.save(existingSubscriptionKeyDomain), AuthorizationDetail.class);
  }

  public void deleteAuthorization(@NotNull String authorizationId) {
    SubscriptionKeyDomain existingSubscriptionKeyDomain = authorizationRepository.findById(authorizationId).orElseThrow(() -> new AppException(AppError.AUTHORIZATION_NOT_FOUND, authorizationId));
    authorizationRepository.delete(existingSubscriptionKeyDomain);
    // TODO triggering the deletion of cached element in async
  }
}
