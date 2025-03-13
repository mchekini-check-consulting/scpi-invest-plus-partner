package fr.formationacademy.scpiinvestpluspartner.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentKafkaDto {
    private String typeProperty;
    private Integer numberShares;
    private Integer numberYears;
    private BigDecimal totalAmount;
    private String scpiName;
    private String investmentState;
    private String investorEmail;

}


