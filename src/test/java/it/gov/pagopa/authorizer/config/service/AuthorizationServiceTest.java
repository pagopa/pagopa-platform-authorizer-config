package it.gov.pagopa.authorizer.config.service;

import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.repository.AuthorizationRepository;
import it.gov.pagopa.authorizer.config.repository.CachedAuthorizationRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

@SpringBootTest(classes = Application.class)
class AuthorizationServiceTest {

    @MockBean private AuthorizationRepository authorizationRepository;

    @MockBean private CachedAuthorizationRepository cachedAuthorizationRepository;

    @Mock private Pageable pageable;

    @Autowired @InjectMocks private AuthorizationService authorizationService;
}
