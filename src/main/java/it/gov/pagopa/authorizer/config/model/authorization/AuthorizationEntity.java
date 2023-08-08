package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;
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
  private String name;

  @JsonProperty("value")
  private String value;

  @JsonProperty("values")
  private List<@NotBlank String> values;
}
