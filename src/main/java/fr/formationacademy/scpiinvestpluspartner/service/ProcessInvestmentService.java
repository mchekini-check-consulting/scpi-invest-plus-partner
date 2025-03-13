package fr.formationacademy.scpiinvestpluspartner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessInvestmentService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInvestmentService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProcessInvestmentService(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public InvestmentState processInvestment(Map<String, Object> dto) {
        if (dto == null || !dto.containsKey("investmentDto") || !dto.containsKey("scpi")) {
            logger.info("REJECTED: Données invalides (investmentDto ou scpi manquant) !");
            return sendInvestmentResponse(InvestmentState.REJECTED, null, null, null);
        }

        Map<String, Object> investmentDto = safeCast(dto.get("investmentDto"));
        Map<String, Object> scpiDto = safeCast(dto.get("scpi"));

        if (investmentDto == null || scpiDto == null) {
            logger.info("REJECTED: Données investmentDto ou scpi non valides !");
            return sendInvestmentResponse(InvestmentState.REJECTED, null, null, null);
        }

        String investorEmail = (String) dto.get("investorEmail");
        String scpiName = (String) scpiDto.get("name");

        BigDecimal totalAmount = new BigDecimal(investmentDto.getOrDefault("totalAmount", "0").toString());
        BigDecimal minimumSubscription = new BigDecimal(scpiDto.getOrDefault("minimumSubscription", "0").toString());

        if (totalAmount.compareTo(minimumSubscription) < 0) {
            return sendInvestmentResponse(InvestmentState.REJECTED, investorEmail, scpiName, investmentDto);
        }

        logger.info("Investissement ACCEPTED pour scpi: {}", scpiName);
        return sendInvestmentResponse(InvestmentState.ACCEPTED, investorEmail, scpiName, investmentDto);
    }

    private InvestmentState sendInvestmentResponse(InvestmentState state, String investorEmail, String scpiName, Map<String, Object> investmentDto) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", state.name());
            response.put("investorEmail", investorEmail);
            response.put("scpiName", scpiName);

            kafkaTemplate.send(Constants.SCPI_PARTNER_RESPONSE_TOPIC, response);
            logger.info("Message de réponse envoyé : {}", response);
        } catch (Exception e) {
            logger.error("Erreur d'envoi Kafka : {}", e.getMessage(), e);
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeCast(Object object) {
        return (object instanceof Map) ? (Map<String, Object>) object : null;
    }
}
