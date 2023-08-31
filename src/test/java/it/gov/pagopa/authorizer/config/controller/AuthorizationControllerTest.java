package it.gov.pagopa.authorizer.config.controller;

import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.model.authorization.*;
import it.gov.pagopa.authorizer.config.service.AuthorizationService;
import it.gov.pagopa.authorizer.config.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class AuthorizationControllerTest {

  @Autowired private MockMvc mvc;

  @MockBean private AuthorizationService authorizationService;

  @ParameterizedTest
  @CsvSource({
          "fakedomain,",
          "fakedomain,77777777777",
  })
  void getAuthorizations_200(String domain, String ownerId) throws Exception {
    String url = String.format("/authorizations?page=0&limit=50&domain=%s&ownerId=%s", domain, ownerId == null ? "" : ownerId);
    // mocking invocation
    when(authorizationService.getAuthorizations(anyString(), anyString(), any(Pageable.class)))
            .thenReturn(TestUtil.getAuthorizations(domain, ownerId));
    // executing API call
    mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getAuthorization_200() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    when(authorizationService.getAuthorization(anyString()))
            .thenReturn(TestUtil.getAuthorization(0, id, "some-domain", "some-owner"));
    // executing API call
    mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getAuthorization_404() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    when(authorizationService.getAuthorization(anyString())).thenThrow(new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION, id));
    // executing API call
    mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void createAuthorization_200() throws Exception {
    String id = "some-uuid";
    String url = "/authorizations/";
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.createAuthorization(any(Authorization.class)))
            .thenReturn(mockedResource);
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(post(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void createAuthorization_400() throws Exception {
    String id = "some-uuid";
    String url = "/authorizations/";
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.createAuthorization(any(Authorization.class))).thenThrow(new AppException(HttpStatus.BAD_REQUEST, "Bad Request", "Some validation error"));
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(post(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }



  @ParameterizedTest
  @CsvSource({
          ",",
          "some,someother"
  })
  void createAuthorization_400_breakingMutualExclusivity(String value, String values) throws Exception {
    String id = "some-uuid";
    String url = "/authorizations/";
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    mockedResource.getAuthorizedEntities().get(0).setValue(value);
    mockedResource.getAuthorizedEntities().get(0).setValues(values != null ? List.of(values) : null);
    when(authorizationService.createAuthorization(any(Authorization.class))).thenThrow(new AppException(HttpStatus.BAD_REQUEST, "Bad Request", "Some validation error"));
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(post(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void createAuthorization_409() throws Exception {
    String id = "some-uuid";
    String url = "/authorizations/";
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.createAuthorization(any(Authorization.class))).thenThrow(new AppException(AppError.CONFLICT_AUTHORIZATION_ALREADY_EXISTENT, "", ""));
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(post(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void createAuthorization_500() throws Exception {
    String id = "some-uuid";
    String url = "/authorizations/";
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.createAuthorization(any(Authorization.class))).thenThrow(new AppException(AppError.INTERNAL_SERVER_ERROR, "", ""));
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(post(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void updateAuthorization_200() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.updateAuthorization(anyString(), any(Authorization.class)))
            .thenReturn(mockedResource);
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(put(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void updateAuthorization_400() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.updateAuthorization(anyString(), any(Authorization.class))).thenThrow(new AppException(HttpStatus.BAD_REQUEST, "Bad Request", "Some validation error"));
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(put(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void updateAuthorization_404() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.updateAuthorization(anyString(), any(Authorization.class))).thenThrow(new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION, id));
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(put(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void updateAuthorization_500() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    Authorization mockedResource = TestUtil.getAuthorization(0, id, "some-domain", "some-owner");
    when(authorizationService.updateAuthorization(anyString(), any(Authorization.class))).thenThrow(new AppException(AppError.INTERNAL_SERVER_ERROR, "", ""));
    // executing API call
    String request = TestUtil.toJson(mockedResource);
    mvc.perform(put(url).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void deleteAuthorization_204() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    doNothing().when(authorizationService).deleteAuthorization(anyString());
    // executing API call
    mvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
  }

  @Test
  void deleteAuthorization_404() throws Exception {
    String id = "some-uuid";
    String url = String.format("/authorizations/%s", id);
    // mocking invocation
    doThrow(new AppException(AppError.NOT_FOUND_NO_VALID_AUTHORIZATION, id)).when(authorizationService).deleteAuthorization(anyString());
    // executing API call
    mvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

}
