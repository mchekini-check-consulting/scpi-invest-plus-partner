package fr.formationacademy.scpiinvestpluspartner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class AcceptedInvestmentResponse {
    private String investorEmail;
    private String scpiName;
    private String propertyType;
    private BigDecimal totalAmount;
    private int numberShares;
    private String bic;
    private String rib;

    public AcceptedInvestmentResponse(
            String investorEmail,
            String scpiName,
            String propertyType,
            BigDecimal totalAmount,
            int numberShares,
            String bic,
            String rib
    ) {
        this.investorEmail = investorEmail;
        this.scpiName = scpiName;
        this.propertyType = propertyType;
        this.totalAmount = totalAmount;
        this.numberShares = numberShares;
        this.bic = bic;
        this.rib = rib;
    }

}

