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
  @Schema(description = "The identifier of the saved authorization, automatically generated during creation as UUID.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String id;

  @JsonProperty("domain")
  @NotBlank(message = "The authorization's domain cannot be null or blank.")
  @Schema(description = "The domain to which the authorization belongs, within which it has validity. Typically, it is defined by choosing from a pool of tags that already exist and are used by the various membership domains.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String domain;

  @JsonProperty("subscription_key")
  @NotBlank(message = "The authorization's subscription key cannot be null or blank.")
  @Schema(description = "The value of the subscription key to be associated with the stored authorization. This key is assigned to an entity that wants to interface with a pagoPA service via APIM, and is the pivotal element on which the Authorizer system will make its evaluations. No two authorizations can exist with the same domain-subscription_key value pair.", requiredMode = Schema.RequiredMode.REQUIRED)
  private String subscriptionKey;

  @JsonProperty("description")
  @Schema(description = "An optional description useful to add more information about the scope of the authorization, defining information also impossible to include in the other tags.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String description;

  @JsonProperty("owner")
  @Valid @NotNull(message = "The owner information must be passed.")
  @Schema(description = "The information about the owner of the authorization. These information are required in order to make maintenance easier and performs some kind of search operations.", requiredMode = Schema.RequiredMode.REQUIRED)
  private AuthorizationOwner owner;

  @JsonProperty("authorized_entities")
  @Valid @NotNull(message = "The authorized entities information must be passed.")
  @Schema(description = "The authorized entity list, which are the resource identifiers that the caller includes in requests that define which objects the entity is authorized to operate on. It consists of a key-value map in which the entity name and its identifier are defined, respectively, in order to make maintenance easier.", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<AuthorizationEntity> authorizedEntities;

  @JsonProperty("other_metadata")
  @Valid @NotNull(message = "The metadata information must be passed.")
  @Schema(description = "The list of authorization metadata, useful for performing other types of computation after the authorization process.", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<AuthorizationMetadata> otherMetadata;

  @JsonProperty(value = "inserted_at", access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The date of authorization entry. This value is set only in authorization creation operations.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String insertedAt;

  @JsonProperty(value = "last_update", access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The date of last authorization update. It is only visible as output in read requests.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String lastUpdate;

  @JsonProperty(value = "last_forced_refresh", access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The date of last forced refresh of the authorization. It is updated only when the forced refresh API is executed.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String lastForcedRefresh;
}
