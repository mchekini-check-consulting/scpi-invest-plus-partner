package fr.formationacademy.scpiinvestpluspartner.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.service.ProcessInvestmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.*;

@Component
@Slf4j
public class InvestmentRequestListener {

    private final ObjectMapper objectMapper;
    private final ProcessInvestmentService processInvestmentService;

    public InvestmentRequestListener(ObjectMapper objectMapper, ProcessInvestmentService processInvestmentService) {
        this.objectMapper = objectMapper;
        this.processInvestmentService = processInvestmentService;
    }

    @KafkaListener(
            topics = SCPI_REQUEST_TOPIC,
            groupId = SCPI_PARTNER_GROUP
    )
    public void investmentRequestListner(ScpiRequestDto data) {
        log.info("message reçu de kafka {}", data);
//        log.info("Message reçu pour l'investissement : {}", record);
//        try {
//            Map<String, Object> dto = objectMapper.readValue(record, new TypeReference<>() {
//            });
//            log.info("Données désérialisées : {}", dto);
//            InvestmentState state = processInvestmentService.processInvestment(dto);
//            dto.put("investmentState", state.name());
//            log.info("Investment processing result: {}", state);
//        } catch (JsonProcessingException e) {
//            log.error("Erreur lors de la désérialisation du message Kafka : {}", e.getMessage());
//        } catch (Exception e) {
//            log.error("Erreur lors du traitement du message Kafka : {}", e.getMessage());
//        }
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
