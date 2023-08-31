package it.gov.pagopa.authorizer.config.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.gov.pagopa.authorizer.config.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class HomeControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  void getHome() throws Exception {
    String url = "/";
    mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void getInfo() throws Exception {
    String url = "/info";
    mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }
}
