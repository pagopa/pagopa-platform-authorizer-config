package it.gov.pagopa.authorizer.config.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class AuthorizedEntity implements Serializable {

  private String name;

  private String value;

  private List<String> values;
}
