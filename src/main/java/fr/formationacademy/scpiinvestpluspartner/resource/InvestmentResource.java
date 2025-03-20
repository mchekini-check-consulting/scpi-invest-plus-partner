package fr.formationacademy.scpiinvestpluspartner.resource;

import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.service.InvestmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/v1/investment")
public class InvestmentResource {

    private final InvestmentService investmentService;

    public InvestmentResource(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }


    @PutMapping("/{id}")
    public ResponseEntity updateInvestment(@PathVariable Integer id, InvestmentState status, String rejectReason) {

        investmentService.updateInvestmentStatus(id, status, rejectReason);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/payment")
    public ResponseEntity updateInvestment(Integer label, BigDecimal amount, String iban, String bic) {

        investmentService.proceedForPayment(label, amount, iban, bic);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
