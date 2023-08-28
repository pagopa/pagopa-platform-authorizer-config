package it.gov.pagopa.authorizer.config.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class GenericPair implements Serializable {

  private String key;

  private String value;

  private List<String> values;
}
