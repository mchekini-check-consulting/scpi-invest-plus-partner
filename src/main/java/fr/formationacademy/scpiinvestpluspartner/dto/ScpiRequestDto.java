package fr.formationacademy.scpiinvestpluspartner.dto;

import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
