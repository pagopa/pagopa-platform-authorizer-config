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

/** Codes associated with Creditor Institution */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CIAssociatedCodeList {

    @JsonProperty("used")
    @Schema(description = "The list of creditor institution's codes, associated to used stations or to other entities.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CIAssociatedCode> usedCodes;

    @JsonProperty("unused")
    @Schema(description = "The list of creditor institution's codes, not associated yet to used stations or to other entities.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<CIAssociatedCode> unusedCodes;
}
