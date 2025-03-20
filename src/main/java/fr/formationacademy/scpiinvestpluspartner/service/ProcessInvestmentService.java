package fr.formationacademy.scpiinvestpluspartner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formationacademy.scpiinvestpluspartner.dto.InvestmentResponse;
import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.entity.Investment;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.mapper.InvestmentMapper;
import fr.formationacademy.scpiinvestpluspartner.repository.InvestmentRepository;
import fr.formationacademy.scpiinvestpluspartner.utils.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState.ACCEPTED;
import static fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState.PROCESSING;
import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.SCPI_PARTNER_RESPONSE_TOPIC;

@Service
@Slf4j
public class ProcessInvestmentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TemplateGeneratorService templateGeneratorService;
    private final InvestmentRepository investmentRepository;
    private final InvestmentMapper investmentMapper;

    public ProcessInvestmentService(KafkaTemplate<String, Object> kafkaTemplate, TemplateGeneratorService templateGeneratorService, InvestmentRepository investmentRepository, InvestmentMapper investmentMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.templateGeneratorService = templateGeneratorService;
        this.investmentRepository = investmentRepository;
        this.investmentMapper = investmentMapper;
    }

    public ScpiRequestDto saveInvestment(ScpiRequestDto data) {
        Investment investment = investmentMapper.toEntity(data);
        data.setInvestmentState(PROCESSING);
        Investment savedInvestment = investmentRepository.save(investment);
        return investmentMapper.toDto(savedInvestment);
    }

    public InvestmentState processInvestment(ScpiRequestDto dto) {
        ValidationResult validationResult = validateInvestment(dto);
        InvestmentState state = mapValidationToInvestmentState(validationResult);
        dto.setInvestmentState(state);
        if (state == InvestmentState.REJECTED) {
            dto.setRejectionReason(getRejectionReason(validationResult));
        }
        sendInvestmentResponse(state, dto, validationResult);
        // generateAndOpenHtml(state, dto, validationResult);
        return state;
    }

    private void generateAndOpenHtml(InvestmentState state, ScpiRequestDto dto, ValidationResult validationResult) {
        String rejectionReason = getRejectionReason(validationResult);
        String amount = Optional.ofNullable(dto.getAmount())
                .map(BigDecimal::toString)
                .orElse("N/A");

        String numberPart = Optional.ofNullable(dto.getNumberYears())
                .map(Object::toString)
                .orElse("N/A");
        String bic = "N/A";
        String rib = "N/A";

        String scpiName = Optional.ofNullable(dto.getScpiName())
                .orElse("N/A");
        Map<String, Object> templateData = Map.of(
                "status", state == ACCEPTED ? "accepted" : "rejected",
                "amount", amount,
                "numberPart", numberPart,
                "bic", bic,
                "rib", rib,
                "scpiName", scpiName,
                "rejectionReason", rejectionReason
        );
        log.info("Données extraites pour le template: {}", templateData);
        try {
            String htmlContent = templateGeneratorService.generateHtml("investment_template", templateData);
            String fileName = "investment_" + (state == ACCEPTED ? "accepted" : "rejected") + "_" + System.currentTimeMillis();
            templateGeneratorService.saveAndOpenHtml(htmlContent, fileName);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du fichier HTML : {}", e.getMessage(), e);
        }
    }

    public InvestmentState mapValidationToInvestmentState(ValidationResult result) {
        return switch (result) {
            case INVALID_DATA, INVALID_INVESTMENT_DTO, INVALID_SCPI_DTO -> InvestmentState.CANCELED;
            case BELOW_MINIMUM_SUBSCRIPTION, ABOVE_SUBSCRIPTION_FEES_THRESHOLD -> InvestmentState.REJECTED;
            case ACCEPTED -> InvestmentState.ACCEPTED;
            default -> throw new IllegalStateException("Unexpected value: " + result);
        };
    }

    public ValidationResult validateInvestment(ScpiRequestDto dto) {
        if (!isValidDto(dto)) {
            return ValidationResult.INVALID_DATA;
        }
        if (!isAboveMinimumSubscription(dto)) {
            return ValidationResult.BELOW_MINIMUM_SUBSCRIPTION;
        }
        return ValidationResult.ACCEPTED;
    }

    private boolean isValidDto(ScpiRequestDto dto) {
        return dto != null && dto.getInvestmentId() != null && dto.getScpiName() != null && dto.getAmount() != null && dto.getInvestorEmail() != null;
    }

    private boolean isAboveMinimumSubscription(ScpiRequestDto dto) {
        BigDecimal totalAmount = dto.getAmount();
        BigDecimal minimumSubscription = new BigDecimal("1000");
        return totalAmount.compareTo(minimumSubscription) >= 0;
    }

    public void sendInvestmentResponse(InvestmentState state, ScpiRequestDto dto, ValidationResult validationResult) {
        try {
            String rejectionReason = state == InvestmentState.REJECTED ? getRejectionReason(validationResult) : null;
            log.info("Rejection reason: {}", rejectionReason);
            InvestmentResponse response = InvestmentResponse.builder()
                    .investmentState(state)
                    .investorEmail(dto.getInvestorEmail())
                    .scpiName(dto.getScpiName())
                    .investmentId(dto.getInvestmentId())
                    .amount(dto.getAmount())
                    .rejectionReason(rejectionReason)
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(response);

            log.info("Données du message Kafka : {}", response);
            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);
            log.info("Message de réponse envoyé avec succès !");
        } catch (Exception e) {
            log.error("Erreur d'envoi Kafka : {}", e.getMessage(), e);
        }
    }

    private String getRejectionReason(ValidationResult result) {
        return switch (result) {
            case INVALID_DATA -> "Les données fournies sont invalides.";
            case BELOW_MINIMUM_SUBSCRIPTION -> "Le montant de souscription est inférieur au minimum requis.";
            default -> "Investissement refusé pour des raisons de validation.";
        };
    }

}