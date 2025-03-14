package fr.formationacademy.scpiinvestpluspartner.service;

import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.utils.Constants;
import fr.formationacademy.scpiinvestpluspartner.utils.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProcessInvestmentService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInvestmentService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TemplateGeneratorService templateGeneratorService;

    public ProcessInvestmentService(KafkaTemplate<String, Object> kafkaTemplate, TemplateGeneratorService templateGeneratorService) {
        this.kafkaTemplate = kafkaTemplate;
        this.templateGeneratorService = templateGeneratorService;
    }

    public InvestmentState processInvestment(Map<String, Object> dto) {
        ValidationResult validationResult = validateInvestment(dto);
        InvestmentState state = mapValidationToInvestmentState(validationResult);
        sendInvestmentResponse(state, dto, validationResult);
        generateAndOpenHtml(state, dto, validationResult);
        return state;
    }

    private void generateAndOpenHtml(InvestmentState state, Map<String, Object> dto, ValidationResult validationResult) {
        String rejectionReason = getRejectionReason(validationResult);

        Map<String, Object> investmentDto = safeCast(dto.get("investmentDto"));
        Map<String, Object> scpi = safeCast(dto.get("scpi"));

        String amount = Optional.ofNullable(investmentDto)
                .map(inv -> inv.get("totalAmount"))
                .map(Object::toString)
                .orElse("N/A");

        String numberPart = Optional.ofNullable(investmentDto)
                .map(inv -> inv.get("numberShares"))
                .map(Object::toString)
                .orElse("N/A");

        String bic = Optional.ofNullable(scpi)
                .map(s -> s.get("bic"))
                .map(Object::toString)
                .orElse("N/A");

        String rib = Optional.ofNullable(scpi)
                .map(s -> s.get("iban"))
                .map(Object::toString)
                .orElse("N/A");

        String scpiName = Optional.ofNullable(scpi)
                .map(s -> s.get("name"))
                .map(Object::toString)
                .orElse("N/A");

        Map<String, Object> templateData = Map.of(
                "status", state == InvestmentState.ACCEPTED ? "accepted" : "rejected",
                "amount", amount,
                "numberPart", numberPart,
                "bic", bic,
                "rib", rib,
                "scpiName", scpiName,
                "rejectionReason", rejectionReason
        );

        logger.info("Données extraites pour le template: {}", templateData);

        try {
            String htmlContent = templateGeneratorService.generateHtml("investment_template", templateData);
            String fileName = "investment_" + (state == InvestmentState.ACCEPTED ? "accepted" : "rejected") + "_" + System.currentTimeMillis();
            templateGeneratorService.saveAndOpenHtml(htmlContent, fileName);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du fichier HTML : {}", e.getMessage(), e);
        }
    }

    private InvestmentState mapValidationToInvestmentState(ValidationResult result) {
        return switch (result) {
            case INVALID_DATA, INVALID_INVESTMENT_DTO, INVALID_SCPI_DTO -> InvestmentState.CANCELED;
            case BELOW_MINIMUM_SUBSCRIPTION, ABOVE_SUBSCRIPTION_FEES_THRESHOLD -> InvestmentState.REJECTED;
            case ACCEPTED -> InvestmentState.ACCEPTED;
            default -> throw new IllegalStateException("Unexpected value: " + result);
        };
    }

    private ValidationResult validateInvestment(Map<String, Object> dto) {
        if (!isValidDto(dto)) {
            return ValidationResult.INVALID_DATA;
        }
        if (!isValidInvestmentDto(dto.get("investmentDto"))) {
            return ValidationResult.INVALID_INVESTMENT_DTO;
        }
        if (!isValidScpiDto(dto.get("scpi"))) {
            return ValidationResult.INVALID_SCPI_DTO;
        }
        if (!isAboveMinimumSubscription(dto)) {
            return ValidationResult.BELOW_MINIMUM_SUBSCRIPTION;
        }
        return ValidationResult.ACCEPTED;
    }

    private boolean isValidDto(Map<String, Object> dto) {
        return dto != null && dto.containsKey("investmentDto") && dto.containsKey("scpi");
    }

    private boolean isValidInvestmentDto(Object investmentDto) {
        return investmentDto instanceof Map;
    }

    private boolean isValidScpiDto(Object scpiDto) {
        return scpiDto instanceof Map;
    }

    private boolean isAboveMinimumSubscription(Map<String, Object> dto) {
        Map<String, Object> investmentDto = safeCast(dto.get("investmentDto"));
        Map<String, Object> scpiDto = safeCast(dto.get("scpi"));
        BigDecimal totalAmount = new BigDecimal(investmentDto.getOrDefault("totalAmount", "0").toString());
        BigDecimal minimumSubscription = new BigDecimal(scpiDto.getOrDefault("minimumSubscription", "0").toString());
        return totalAmount.compareTo(minimumSubscription) >= 0;
    }

    private String getRejectionReason(ValidationResult result) {
        return switch (result) {
            case INVALID_DATA -> "Les données fournies sont invalides.";
            case INVALID_INVESTMENT_DTO -> "Les informations d'investissement sont incorrectes.";
            case INVALID_SCPI_DTO -> "Les informations SCPI sont incorrectes.";
            case BELOW_MINIMUM_SUBSCRIPTION -> "Le montant de souscription est inférieur au minimum requis.";
            default -> "Investissement refusé pour des raisons de validation.";
        };
    }

    private void sendInvestmentResponse(InvestmentState state, Map<String, Object> dto, ValidationResult validationResult) {
        try {
            String rejectionReason = state == InvestmentState.REJECTED ? getRejectionReason(validationResult) : null;
            Map<String, Object> scpi = safeCast(dto.get("scpi"));

            assert rejectionReason != null;
            Map<String, Object> response = new HashMap<>();
            response.put("status", state.name());
            response.put("investorEmail", dto.get("investorEmail"));
            response.put("scpiName", Optional.ofNullable(scpi)
                    .map(s -> s.get("name"))
                    .map(Object::toString)
                    .orElse("N/A"));
            response.put("investmentDto", dto.get("investmentDto"));

            if (rejectionReason != null) {
                response.put("rejectionReason", rejectionReason);
            }
            logger.info("Données du message Kafka : {}", response);
            kafkaTemplate.send(Constants.SCPI_PARTNER_RESPONSE_TOPIC, response);
            logger.info("Message de réponse envoyé avec succès !");
        } catch (Exception e) {
            logger.error("Erreur d'envoi Kafka : {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeCast(Object object) {
        return (object instanceof Map) ? (Map<String, Object>) object : null;
    }
}