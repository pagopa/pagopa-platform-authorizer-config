package it.gov.pagopa.authorizer.config.model.organization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrolledCreditorInstitution {

    @JsonProperty("organization_fiscal_code")
    @Schema(description = "The fiscal code related to the creditor institution.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String organizationFiscalCode;

    @JsonProperty("segregation_codes")
    @Schema(description = "The list of segregation codes used by the creditor institution to register a station for the required service domain.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> segregationCodes;
}
