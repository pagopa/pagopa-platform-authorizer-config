package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  private String key;

  @JsonProperty("value")
  private String value;

  @JsonProperty("values")
  private List<@NotBlank String> values;
}
