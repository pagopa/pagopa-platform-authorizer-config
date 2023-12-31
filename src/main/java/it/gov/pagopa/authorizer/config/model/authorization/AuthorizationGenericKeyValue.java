package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.authorizer.config.validation.annotation.MutuallyExclusiveFields;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MutuallyExclusiveFields(fields = { "value", "values" }, canBeBothNull = false)
public class AuthorizationGenericKeyValue implements Serializable {

  @JsonProperty("key")
  @NotBlank
  @Schema(description = "The key used to reference the metadata into the related map.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String key;

  @JsonProperty("value")
  @Schema(description = "The single simple value related to the metadata. Only one between 'value' and 'values' tag at a time can exists in this object.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String value;

  @JsonProperty("values")
  @Schema(description = "The set of values related to the metadata. Only one between 'value' and 'values' tag at a time can exists in this object.", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<@NotBlank String> values;
}
