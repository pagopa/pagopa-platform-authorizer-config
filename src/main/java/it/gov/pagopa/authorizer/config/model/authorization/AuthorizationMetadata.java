package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizationMetadata implements Serializable {

  @JsonProperty("name")
  @NotBlank
  @Schema(description = "A description that defines the full name of the metadata.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @JsonProperty("short_key")
  @NotBlank @Pattern(regexp = "_[a-zA-Z0-9]{1,3}", message = "The field 'short_key' must start with an underscore and can contains a maximum of 3 other alphanumeric characters.")
  @Schema(description = "The key that defines an abbreviation by which it will be identified in cached maps.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String shortKey;

  @JsonProperty("content")
  @NotNull
  @Schema(description = "A key-value map that defines the actual content of the metadata to be stored.", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<AuthorizationGenericKeyValue> content;
}
