package it.gov.pagopa.authorizer.config.model.cachedauthorization;


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

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class CachedAuthorization implements Serializable {

  @JsonProperty("description")
  @Schema(description = "The description that is associated with particular noteworthy items to be added to the list of cached information.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String description;

  @JsonProperty("owner")
  @Schema(description = "The identifier of the authorization owner. This can be the fiscal code of the entity/intermediary or other information that uniquely identifies that entity.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String owner;

  @JsonProperty("subscription_key")
  @Schema(description = "The value of the subscription key associated with the cached authorization.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String subscriptionKey;

  @JsonProperty("ttl")
  @NotNull
  @Schema(description = "The remaining Time-to-Live related to the cached authorization. This can be formatted either in seconds format or in a particular format that follows the structure 'XXh YYm ZZs'.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String ttl;
}
