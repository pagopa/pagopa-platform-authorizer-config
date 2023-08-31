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
import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizationOwner implements Serializable {

  @JsonProperty("id")
  @NotBlank
  @Schema(description = "The identifier of the authorization owner. This can be the fiscal code of the entity/intermediary or other information that uniquely identifies that entity.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String id;

  @JsonProperty("name")
  @NotBlank
  @Schema(description = "The name of the authorization owner, useful in order to make an authorization more human-readable. It can be the entity's business name or any other information that helps its recognition.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @JsonProperty("type")
  @NotNull
  @Schema(description = "The authorization owner type, useful both for adding an additional recognizable 'label' to the subject and for use as a search filter.", requiredMode = Schema.RequiredMode.REQUIRED)
  private OwnerType type;
}
