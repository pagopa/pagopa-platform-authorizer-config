package it.gov.pagopa.authorizer.config.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GenericPair implements Serializable {

  private String key;

  private String value;

  private List<String> values;
}
