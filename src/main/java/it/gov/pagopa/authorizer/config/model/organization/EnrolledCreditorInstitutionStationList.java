package it.gov.pagopa.authorizer.config.model.organization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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
public class EnrolledCreditorInstitutionStationList {

    @JsonProperty("stations")
    @NotNull
    @Schema(description = "The list of station for the passed creditor institution enrolled to the Authorizer service for the required service domain.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<EnrolledCreditorInstitutionStation> stations;
}
