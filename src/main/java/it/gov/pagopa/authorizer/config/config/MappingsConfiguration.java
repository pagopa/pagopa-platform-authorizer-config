package it.gov.pagopa.authorizer.config.config;

import it.gov.pagopa.authorizer.config.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.config.mapper.AuthorizationDetailToSubscriptionKeyDomainConverter;
import it.gov.pagopa.authorizer.config.mapper.SubscriptionKeyDomainToAuthorizationDetailConverter;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationDetail;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingsConfiguration {

  @Bean
  ModelMapper modelMapper() {
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    Converter<AuthorizationDetail, SubscriptionKeyDomain> authorizationDetailToSubscriptionKeyDomainConverter = new AuthorizationDetailToSubscriptionKeyDomainConverter();
    mapper.createTypeMap(AuthorizationDetail.class, SubscriptionKeyDomain.class).setConverter(authorizationDetailToSubscriptionKeyDomainConverter);

    Converter<SubscriptionKeyDomain, AuthorizationDetail> subscriptionKeyDomainToAuthorizationDetailConverter = new SubscriptionKeyDomainToAuthorizationDetailConverter();
    mapper.createTypeMap(SubscriptionKeyDomain.class, AuthorizationDetail.class).setConverter(subscriptionKeyDomainToAuthorizationDetailConverter);

    return mapper;
  }

}
