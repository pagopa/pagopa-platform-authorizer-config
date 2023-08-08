package it.gov.pagopa.authorizer.config.model.cachedauthorization;


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

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class CachedAuthorization {

  @JsonProperty("description")
  private String description;

  @JsonProperty("owner")
  private String owner;

  @JsonProperty("subscription_key")
  private String subscriptionKey;

  @JsonProperty("ttl")
  private String ttl;
}
