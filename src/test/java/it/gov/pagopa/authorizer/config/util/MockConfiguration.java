package it.gov.pagopa.authorizer.config.util;

import com.azure.spring.data.cosmos.common.ExpressionResolver;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.repository.CachedAuthorizationRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MockConfiguration {

  @Bean
  @Primary
  ExpressionResolver expressionResolver() {
    return Mockito.mock(ExpressionResolver.class);
  }

  @Bean
  @Primary
  AuthorizationRepository authorizationRepository() {
    return Mockito.mock(AuthorizationRepository.class);
  }
}
