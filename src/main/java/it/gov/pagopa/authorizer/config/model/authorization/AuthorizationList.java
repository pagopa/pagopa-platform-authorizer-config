package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.authorizer.config.model.PageInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizationList implements Serializable {

  @JsonProperty("authorizations")
  @NotNull
  @Schema(description = "The list of authorization retrieved from search.", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<Authorization> authorizations;

  @JsonProperty("page_info")
  @Valid @NotNull
  @Schema(description = "The information related to the paginated results.", requiredMode = Schema.RequiredMode.REQUIRED)
  private PageInfo pageInfo;
}
