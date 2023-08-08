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

  private String name;

  private String shortKey;

  private List<GenericPair> content;
}
