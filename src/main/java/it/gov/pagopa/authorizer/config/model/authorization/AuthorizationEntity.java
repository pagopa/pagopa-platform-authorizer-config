package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.authorizer.config.validation.annotation.MutuallyExclusiveFields;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@MutuallyExclusiveFields(fields = { "value", "values" }, canBeBothNull = false)
public class AuthorizationEntity implements Serializable {

  @JsonProperty("name")
  @NotBlank
  @Schema(description = "The name or the description associated to the authorization entity in order to reference it in a more human-readable mode.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @JsonProperty("value")
  @Schema(description = "The single simple value related to an entity to be authorized to access within an authorization. Only one between 'value' and 'values' tag at a time can exists in this object.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String value;

  @JsonProperty("values")
  @Schema(description = "The multiple composite sub-values which concatenation forms a complex entity to be authorized to access within an authorization. Only one between 'value' and 'values' tag at a time can exists in this object.", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<@NotBlank String> values;
}
