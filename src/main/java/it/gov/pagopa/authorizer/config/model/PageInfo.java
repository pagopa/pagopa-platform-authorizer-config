package it.gov.pagopa.authorizer.config.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageInfo implements Serializable {

  @JsonProperty("page")
  @PositiveOrZero
  @Schema(description = "The page number", requiredMode = Schema.RequiredMode.REQUIRED)
  Integer page;

  @JsonProperty("limit")
  @Positive
  @Schema(description = "The required maximum number of items per page", requiredMode = Schema.RequiredMode.REQUIRED)
  Integer limit;

  @JsonProperty("items_found")
  @PositiveOrZero
  @Schema(description = "The number of items found. (The last page may have fewer elements than required)", requiredMode = Schema.RequiredMode.REQUIRED)
  Integer itemsFound;

  @JsonProperty("total_pages")
  @PositiveOrZero
  @Schema(description = "The total number of pages", requiredMode = Schema.RequiredMode.REQUIRED)
  Integer totalPages;
}
