package it.gov.pagopa.authorizer.config.controller;

import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.service.AuthorizationService;
import it.gov.pagopa.authorizer.config.util.MockBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class CachedAuthorizationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorizationService authorizationService;

    @ParameterizedTest
    @CsvSource({
            "fakedomain,,",
            "fakedomain,true,",
            "fakedomain,false,",
            "fakedomain,true,77777777777",
            "fakedomain,false,77777777777",
    })
    void getAuthorizations_200(String domain, Boolean formatTTL, String ownerId) throws Exception {
        String url = String.format("/cachedauthorizations?domain=%s&ownerId=%s&formatTTL=%s", domain, ownerId == null ? "" : ownerId, formatTTL == null ? "" : formatTTL);
        // mocking invocation
        when(authorizationService.getCachedAuthorization(anyString(), anyString(), anyBoolean()))
                .thenReturn(MockBuilder.getCachedAuthorizationList(domain, ownerId, formatTTL == null || formatTTL));
        // executing API call
        mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @CsvSource({
            "fakedomain,,",
            "fakedomain,77777777777",
    })
    void refreshCachedAuthorizations_200(String domain, String ownerId) throws Exception {
        String url = String.format("/cachedauthorizations/%s/refresh?ownerId=%s", domain, ownerId == null ? "" : ownerId);
        // mocking invocation
        doNothing().when(authorizationService).refreshCachedAuthorizations(anyString(), anyString());
        // executing API call
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void refreshCachedAuthorizations_500() throws Exception {
        String url = String.format("/cachedauthorizations/%s/refresh?ownerId=%s", "fakedomain", "77777777777");
        // mocking invocation
        doThrow(new AppException(AppError.INTERNAL_SERVER_ERROR_REFRESH)).when(authorizationService).refreshCachedAuthorizations(anyString(), anyString());
        // executing API call
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
