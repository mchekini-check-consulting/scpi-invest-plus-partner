package fr.formationacademy.scpiinvestpluspartner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class RejectedInvestmentResponse {
    private String investorEmail;
    private String scpiName;
    private String rejectionReason;

    public RejectedInvestmentResponse(String investorEmail, String scpiName, String rejectionReason) {
        this.investorEmail = investorEmail;
        this.scpiName = scpiName;
        this.rejectionReason = rejectionReason;
    }
}
