package it.gov.pagopa.authorizer.config.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.authorizer.config.model.authorization.AuthorizationGenericKeyValue;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Metadata implements Serializable {

  @JsonProperty("name")
  private String name;

  @JsonProperty("short_key")
  private String shortKey;

  @JsonProperty("content")
  private List<GenericPair> content;
}
