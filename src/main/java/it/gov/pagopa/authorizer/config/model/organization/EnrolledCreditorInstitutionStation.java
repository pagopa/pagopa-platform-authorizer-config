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

import javax.validation.constraints.NotNull;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrolledCreditorInstitutionStation {

    @JsonProperty("station_id")
    @NotNull
    @Schema(description = "The identifier of the station related to the creditor institution.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stationId;

    @JsonProperty("segregation_code")
    @NotNull
    @Schema(description = "The segregation code used by the creditor institution to register a station for the required service domain.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String segregationCode;
}
