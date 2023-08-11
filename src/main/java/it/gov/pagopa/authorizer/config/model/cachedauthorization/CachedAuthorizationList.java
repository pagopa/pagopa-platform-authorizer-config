package it.gov.pagopa.authorizer.config.model.cachedauthorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CachedAuthorizationList implements Serializable {

  @JsonProperty("cached_authorizations")
  @NotNull
  @Schema(description = "The list of authorization cached in Authorizer system.", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<CachedAuthorization> cachedAuthorizations;
}
