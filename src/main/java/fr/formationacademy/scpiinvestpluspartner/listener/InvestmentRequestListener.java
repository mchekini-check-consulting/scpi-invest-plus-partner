package fr.formationacademy.scpiinvestpluspartner.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.service.ProcessInvestmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
            topics = SCPI_REQUEST_TOPIC,
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
    public void investmentResponseListener(String message) throws JsonProcessingException {
        log.info("Message reçu : {}", message);
        JsonNode jsonNode = objectMapper.readTree(message);
        String status = jsonNode.get("status").asText();
        String investorEmail = jsonNode.get("investorEmail").asText();
        String scpiName = jsonNode.has("scpiName") ? jsonNode.get("scpiName").asText() : "N/A";
        log.info("Traitement de la demande : Status={}, SCPI={}, Email={}", status, scpiName, investorEmail);
    }

}
