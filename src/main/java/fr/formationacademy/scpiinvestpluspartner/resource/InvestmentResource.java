package fr.formationacademy.scpiinvestpluspartner.resource;

import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/v1/investment")
@Tag(name="Partner Investment", description = "Gère les demandes d'investissements reçues par le service partenaire")
public class InvestmentResource {

    private final InvestmentService investmentService;

    public InvestmentResource(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @Operation(
            description = "Modifier l'état de la demande d'investissement."
    )
    @PutMapping("/{id}")
    public ResponseEntity updateInvestment(@PathVariable Integer id, InvestmentState status, String rejectReason) {

        investmentService.updateInvestmentStatus(id, status, rejectReason);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            description = "Modifie l'état de payement de la demande."
    )
    @PutMapping("/payment")
    public ResponseEntity updateInvestment(Integer label, BigDecimal amount, String iban, String bic) {

        investmentService.proceedForPayment(label, amount, iban, bic);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
