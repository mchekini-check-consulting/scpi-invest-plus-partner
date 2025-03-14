package fr.formationacademy.scpiinvestpluspartner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.*;

@Component
public class InvestmentRequestListener {

    private static final Logger log = LogManager.getLogger(InvestmentRequestListener.class);

    private final ObjectMapper objectMapper;
    private final ProcessInvestmentService processInvestmentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InvestmentRequestListener(ObjectMapper objectMapper, ProcessInvestmentService processInvestmentService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.processInvestmentService = processInvestmentService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = SCPI_PARTNER_TOPIC,
            groupId = SCPI_PARTNER_GROUP
    )
    public void investmentRequestListner(String record) {
        log.info("Message reçu pour l'investissement : {}", record);
        try {
            Map<String, Object> dto = objectMapper.readValue(record, new TypeReference<>() {
            });
            log.info("Données désérialisées : {}", dto);
            InvestmentState state = processInvestmentService.processInvestment(dto);
            dto.put("investmentState", state.name());
            log.info("Investment processing result: {}", state);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la désérialisation du message Kafka : {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message Kafka : {}", e.getMessage());
        }
    }

    @KafkaListener(
            topics = SCPI_PARTNER_RESPONSE_TOPIC,
            groupId = SCPI_PARTNER_GROUP
    )
    public void InvestmentResponseListner(String message) {
        log.info("Starting the treatment of the demand : {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            System.out.println("Investment processing result: " + jsonNode.get("status").asText());
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }

    private void handleAcceptedInvestment(JsonNode dto, String investorEmail, String scpiName) {
        try {
            Map<String, Object> response = Map.of(
                    "investorEmail", investorEmail,
                    "scpiName", scpiName,
                    "propertyType", dto.path("propertyType").asText(),
                    "totalAmount", new BigDecimal(dto.path("totalAmount").asText()),
                    "numberShares", dto.path("numberShares").asInt(),
                    "bic", dto.path("bic").asText(),
                    "rib", dto.path("rib").asText()
            );

            log.info("Investissement accepté : {}", response);
            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'investissement accepté : {}", e.getMessage(), e);
        }
    }

    private void handleRejectedInvestment(JsonNode dto, String investorEmail, String scpiName) {
        try {
            Map<String, Object> response = Map.of(
                    "investorEmail", investorEmail,
                    "scpiName", scpiName,
                    "reason", dto.path("reason").asText()
            );

            log.info("Investissement rejeté : {}", response);
            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'investissement rejeté : {}", e.getMessage(), e);
        }
    }
}
