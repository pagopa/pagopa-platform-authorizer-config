package it.gov.pagopa.authorizer.config.model.organization;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Code associated with Creditor Institution */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CIAssociatedCode {

    @JsonProperty("code")
    @Schema(description = "The code associated to the creditor institution.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @JsonProperty("name")
    @Schema(description = "The name of the entity associated to the creditor institution by the code.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String stationName;
}
