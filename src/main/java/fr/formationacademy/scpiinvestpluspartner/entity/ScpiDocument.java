package fr.formationacademy.scpiinvestpluspartner.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "scpi")
public class ScpiDocument {

    @Id
    private String id;
    private Integer scpiId;
    private String name;
    private String iban;
    private String bic;
    private BigDecimal sharePrice;

}
