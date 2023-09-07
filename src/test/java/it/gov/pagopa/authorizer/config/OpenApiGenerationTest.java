package it.gov.pagopa.authorizer.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class OpenApiGenerationTest {

  private static final String[][] apiDocsGroups = {
      { "", "/v3/api-docs/general" },
      { "_core", "/v3/api-docs/crud"},
      { "_enrolledci", "/v3/api-docs/enrolled_ci" }
  };

  @Autowired ObjectMapper objectMapper;

  @Autowired private MockMvc mvc;

  @Test
  void swaggerSpringPlugin() throws Exception {
    for (String[] apiDocs : apiDocsGroups) {
      mvc.perform(MockMvcRequestBuilders.get(apiDocs[1]).accept(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
          .andDo(
              (result) -> {
                assertNotNull(result);
                assertNotNull(result.getResponse());
                final String content = result.getResponse().getContentAsString();
                assertFalse(content.isBlank());
                assertFalse(content.contains("${"), "Generated swagger contains placeholders");
                Object swagger = objectMapper.readValue(result.getResponse().getContentAsString(), Object.class);
                String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
                Path basePath = Paths.get("openapi/");
                Files.createDirectories(basePath);
                Files.write(basePath.resolve(String.format("openapi%s.json", apiDocs[0])), formatted.getBytes());
              });
    }
  }
}
