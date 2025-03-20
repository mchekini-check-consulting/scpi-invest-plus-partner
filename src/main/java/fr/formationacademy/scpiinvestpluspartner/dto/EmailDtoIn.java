package fr.formationacademy.scpiinvestpluspartner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDtoIn {

    private String from;
    private String to;
    private String subject;
    private String body;
    private String bodyType;
}
