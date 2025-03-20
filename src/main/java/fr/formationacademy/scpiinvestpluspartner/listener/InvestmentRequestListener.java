package fr.formationacademy.scpiinvestpluspartner.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.service.ProcessInvestmentService;
import fr.formationacademy.scpiinvestpluspartner.utils.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.*;

@Component
@Slf4j
public class InvestmentRequestListener {
    private final ProcessInvestmentService processInvestmentService;

    public InvestmentRequestListener(ProcessInvestmentService processInvestmentService) {
        this.processInvestmentService = processInvestmentService;
    }

    @KafkaListener(
            topics = SCPI_REQUEST_TOPIC,
            groupId = SCPI_PARTNER_GROUP
    )
    public void investmentRequestListener(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ScpiRequestDto data = objectMapper.readValue(message, ScpiRequestDto.class);
            log.info("Message reçu : {}", data);
            InvestmentState state = processInvestmentService.processInvestment(data);
            log.info("Réponse du traitement de l'investissement : {}", data);
            log.info("Nouveau status aprés traitement de l'investissement : {}", state);
        } catch (Exception e) {
            log.error("Erreur de conversion du message Kafka : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = SCPI_PARTNER_RESPONSE_TOPIC,
            groupId = SCPI_PARTNER_GROUP
    )
    public void investmentResponseListener(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ScpiRequestDto response = objectMapper.readValue(message, ScpiRequestDto.class);
            log.info("Message reçu et converti : {}", response);
            InvestmentState status = response.getInvestmentState();
            String investorEmail = response.getInvestorEmail();
            String scpiName = response.getScpiName() != null ? response.getScpiName() : "N/A";
            //String rejectionReason = response.getRejectionReason();
            log.info("Traitement de la demande : Status={}, SCPI={}, Email={}", status, scpiName, investorEmail);
            ValidationResult validationResult = processInvestmentService.validateInvestment(response);
            processInvestmentService.sendInvestmentResponse(status, response, validationResult);
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message d'investissement : {}", e.getMessage(), e);
        }
    }
}
