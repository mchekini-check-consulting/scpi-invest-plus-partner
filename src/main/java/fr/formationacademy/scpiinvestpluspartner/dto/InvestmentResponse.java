package fr.formationacademy.scpiinvestpluspartner.dto;

import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvestmentResponse {
    private InvestmentState investmentState;
    private String investorEmail;
    private String scpiName;
    private Integer investmentId;
    private BigDecimal amount;
    private String rejectionReason;
}
