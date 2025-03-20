package fr.formationacademy.scpiinvestpluspartner.listener;

import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.entity.Investment;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.repository.InvestmentRepository;
import fr.formationacademy.scpiinvestpluspartner.service.ProcessInvestmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.*;

@Component
@Slf4j
public class InvestmentRequestListener {
    private final ProcessInvestmentService processInvestmentService;
    private final InvestmentRepository investmentRepository;

    public InvestmentRequestListener(ProcessInvestmentService processInvestmentService, InvestmentRepository investmentRepository) {
        this.processInvestmentService = processInvestmentService;
        this.investmentRepository = investmentRepository;
    }

    @KafkaListener(
            topics = SCPI_REQUEST_TOPIC,
            groupId = SCPI_PARTNER_GROUP
    )
    public void investmentRequestListener(ScpiRequestDto data) {
        log.info("Message reçu de Kafka : {}", data);

        try {
            // Sauvegarder l'investissement initial
            ScpiRequestDto response = processInvestmentService.saveInvestment(data);
            Investment savedInvestment = investmentRepository.findById(response.getInvestmentId()).orElse(null);

            if (savedInvestment != null) {
                log.info("Investissement correctement enregistré : {}", savedInvestment);
            } else {
                log.error("Échec de l'enregistrement de l'investissement avec l'ID : {}", response.getInvestmentId());
                return;
            }
            InvestmentState state = processInvestmentService.processInvestment(data);
            log.info("Réponse du traitement de l'investissement : {}", response);
            savedInvestment.setInvestmentState(state);
            investmentRepository.save(savedInvestment);
            log.info("Statut de l'investissement mis à jour : {}", state);
            log.info("Résultat du traitement de l'investissement : {}", state);
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message d'investissement : {}", e.getMessage(), e);
        }
    }


    @KafkaListener(
            topics = SCPI_PARTNER_RESPONSE_TOPIC,
            groupId = SCPI_PARTNER_GROUP
    )
    public void investmentResponseListener(ScpiRequestDto response) {
        log.info("Message reçu : {}", response);

        try {
            InvestmentState status = response.getInvestmentState();
            String investorEmail = response.getInvestorEmail();
            String scpiName = response.getScpiName() != null ? response.getScpiName() : "N/A";
            log.info("Traitement de la demande : Status={}, SCPI={}, Email={}", status, scpiName, investorEmail);
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message d'investissement : {}", e.getMessage(), e);
        }
    }
}
