package fr.formationacademy.scpiinvestpluspartner.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.service.ProcessInvestmentService;
import fr.formationacademy.scpiinvestpluspartner.utils.TopicNameProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.SCPI_PARTNER_GROUP;
import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.SCPI_REQUEST_TOPIC;

@Component
@Slf4j
public class InvestmentRequestListener {
    private final ProcessInvestmentService processInvestmentService;
    private final TopicNameProvider topicNameProvider;
    private final String scpiInvestRequestTopic;
    public InvestmentRequestListener(ProcessInvestmentService processInvestmentService, TopicNameProvider topicNameProvider) {
        this.processInvestmentService = processInvestmentService;
        this.topicNameProvider = topicNameProvider;
        this.scpiInvestRequestTopic = topicNameProvider.getScpiInvestRequestTopic();
    }

    @KafkaListener(
            topics = "#{topicNameProvider.getScpiInvestRequestTopic()}",
            groupId = SCPI_PARTNER_GROUP
    )
    public void investmentRequestListener(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ScpiRequestDto data = objectMapper.readValue(message, ScpiRequestDto.class);
            log.info("Message reçu : {}", data);
            processInvestmentService.processInvestment(data);
        } catch (Exception e) {
            log.error("Erreur de conversion du message Kafka : {}", e.getMessage(), e);
        }
    }
}
