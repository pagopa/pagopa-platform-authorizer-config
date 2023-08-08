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
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Authorization implements Serializable {

  @JsonProperty("id")
  private String id;

  @NotBlank(message = "The authorization's domain cannot be null or blank.")
  @JsonProperty("domain")
  private String domain;

  @NotBlank(message = "The authorization's subscription key cannot be null or blank.")
  @JsonProperty("subscription_key")
  private String subscriptionKey;

  @JsonProperty("description")
  private String description;

  @Valid
  @NotNull(message = "The owner information must be passed.")
  @JsonProperty("owner")
  private AuthorizationOwner owner;

  @Valid
  @NotNull(message = "The authorized entities information must be passed.")
  @JsonProperty("authorized_entities")
  private List<AuthorizationEntity> authorizedEntities;

  @Valid
  @NotNull(message = "The metadata information must be passed.")
  @JsonProperty("other_metadata")
  private List<AuthorizationMetadata> otherMetadata;

  @JsonProperty("inserted_at")
  private String insertedAt;

  @JsonProperty("last_update")
  private String lastUpdate;

  @JsonProperty("last_forced_refresh")
  private String lastForcedRefresh;
}
