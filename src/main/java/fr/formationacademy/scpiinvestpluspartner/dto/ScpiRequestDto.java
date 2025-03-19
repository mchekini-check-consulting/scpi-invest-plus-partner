package fr.formationacademy.scpiinvestpluspartner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScpiRequestDto {

    private Integer investmentId;
    private String name;
    private BigDecimal amount;
    private String investorEmail;
    private String propertyType;
    private Integer numberYears;
}

