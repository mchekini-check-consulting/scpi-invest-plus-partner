/*package fr.formationacademy.scpiinvestpluspartner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.models.InvestmentDtoOut;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class KakfaConsumerService {
    private static final Logger log = LogManager.getLogger(KakfaConsumerService.class);
    private final ObjectMapper objectMapper;

    public KakfaConsumerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "scpi-partner-topic",
            groupId = "scpi-partner-group"
    )
    public void listen(String record) throws JsonProcessingException {
        log.info("Received record: {} ", record);
        InvestmentDtoOut recievedSimulationInDTO = objectMapper.readValue(record, InvestmentDtoOut.class);
        log.info("Received record: {} ", recievedSimulationInDTO);
    }
}
*/