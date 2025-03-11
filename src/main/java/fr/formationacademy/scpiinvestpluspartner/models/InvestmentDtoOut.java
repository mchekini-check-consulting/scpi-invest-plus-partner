package fr.formationacademy.scpiinvestpluspartner.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentDtoOut {
    private String typeProperty;
    private Integer numberShares;
    private Integer numberYears;
    private BigDecimal totalAmount;
    private String scpiName;
    private String investmentState;
}


