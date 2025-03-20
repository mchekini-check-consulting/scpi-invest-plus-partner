package fr.formationacademy.scpiinvestpluspartner.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Builder
public class ScpiRequestDto {
    private Integer investmentId;
    private String scpiName;
    private BigDecimal amount;
    private String investorEmail;
    private String propertyType;
    private Integer numberYears;
    private InvestmentState investmentState;
    private String rejectionReason;

    @JsonCreator
    public ScpiRequestDto(
            @JsonProperty("investmentId") Integer investmentId,
            @JsonProperty("scpiName") String scpiName,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("investorEmail") String investorEmail,
            @JsonProperty("propertyType") String propertyType,
            @JsonProperty("numberYears") Integer numberYears,
            @JsonProperty("investmentState") InvestmentState investmentState,
            @JsonProperty("rejectionReason") String rejectionReason) {

        this.investmentId = investmentId;
        this.scpiName = scpiName;
        this.amount = amount;
        this.investorEmail = investorEmail;
        this.propertyType = propertyType;
        this.numberYears = numberYears;
        this.investmentState = investmentState;
        this.rejectionReason = rejectionReason;
    }
}
