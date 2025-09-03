package it.gov.pagopa.authorizer.config.model.authorization;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonPropertyOrder({"created_at", "domain", "size", "authorized_entities"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedEntityList implements Serializable {

  @JsonProperty("created_at")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "The time on which the cached value was created.")
  private LocalDateTime createdAt;

  @JsonProperty("domain")
  @Schema(description = "The domain related to the authorized entities.")
  private String domain;

  @JsonProperty("size")
  @Schema(description = "The number of authorized entities stored.")
  private Integer size;

  @JsonProperty("authorized_entities")
  @NotNull
  @Schema(description = "The list of authorized entities retrieved from search in DB.", requiredMode = Schema.RequiredMode.REQUIRED)
  private Set<String> authorizedEntities;
}
