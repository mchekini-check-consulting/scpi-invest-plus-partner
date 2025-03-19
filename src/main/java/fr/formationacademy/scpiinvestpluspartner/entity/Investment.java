package fr.formationacademy.scpiinvestpluspartner.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "Investment")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Investment {
    @Id
    private Integer investmentId;
    private String propertyType;
    private Integer numberYears;
    private BigDecimal amount;
    private String investorEmail;
    private String investmentState;
    private String scpiName;
}
