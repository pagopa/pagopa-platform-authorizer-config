package it.gov.pagopa.authorizer.config.controller;

import it.gov.pagopa.authorizer.config.Application;
import it.gov.pagopa.authorizer.config.exception.AppError;
import it.gov.pagopa.authorizer.config.exception.AppException;
import it.gov.pagopa.authorizer.config.service.EnrolledOrganizationService;
import it.gov.pagopa.authorizer.config.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class EnrolledOrganizationControllerTest {

    @Autowired private MockMvc mvc;

    @MockBean private EnrolledOrganizationService enrolledOrganizationService;


    @Test
    void getEnrolledOrganizations_200() throws Exception {
        String url = "/organizations/domains/fakedomain";
        // mocking invocation
        when(enrolledOrganizationService.getEnrolledOrganizations(anyString()))
                .thenReturn(TestUtil.getEnrolledCreditorInstitutionList());
        // executing API call
        mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getEnrolledOrganizations_500() throws Exception {
        String url = "/organizations/domains/fakedomain";
        // mocking invocation
        when(enrolledOrganizationService.getEnrolledOrganizations(anyString()))
                .thenThrow(new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        // executing API call
        mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getStationsForEnrolledOrganizations_200() throws Exception {
        String url = "/organizations/77777777777/domains/fakedomain";
        // mocking invocation
        when(enrolledOrganizationService.getStationsForEnrolledOrganizations(anyString(), anyString()))
                .thenReturn(TestUtil.getEnrolledCreditorInstitutionStationList());
        // executing API call
        mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getStationsForEnrolledOrganizations_400() throws Exception {
        String url = "/organizations/77777777777/domains/fakedomain";
        // mocking invocation
        when(enrolledOrganizationService.getStationsForEnrolledOrganizations(anyString(), anyString()))
                .thenThrow(new AppException(AppError.BAD_REQUEST_WILDCARD_ORG));
        // executing API call
        mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getStationsForEnrolledOrganizations_404() throws Exception {
        String url = "/organizations/77777777777/domains/fakedomain";
        // mocking invocation
        when(enrolledOrganizationService.getStationsForEnrolledOrganizations(anyString(), anyString()))
                .thenThrow(new AppException(AppError.NOT_FOUND_CI_NOT_ENROLLED, "77777777777", "fakedomain"));
        // executing API call
        mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getStationsForEnrolledOrganizations_500() throws Exception {
        String url = "/organizations/77777777777/domains/fakedomain";
        // mocking invocation
        when(enrolledOrganizationService.getStationsForEnrolledOrganizations(anyString(), anyString()))
                .thenThrow(new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        // executing API call
        mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
