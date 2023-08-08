package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  private String name;

  @JsonProperty("short_key")
  @NotBlank
  @Pattern(regexp = "_[a-zA-Z0-9]{1,3}", message = "The field 'short_key' must start with an underscore and can contains a maximum of 3 other alphanumeric characters.")
  private String shortKey;

  @JsonProperty("content")
  @NotNull
  private List<AuthorizationGenericKeyValue> content;
}
