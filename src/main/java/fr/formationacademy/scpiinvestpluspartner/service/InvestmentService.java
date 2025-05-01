package fr.formationacademy.scpiinvestpluspartner.service;

import fr.formationacademy.scpiinvestpluspartner.dto.EmailDtoIn;
import fr.formationacademy.scpiinvestpluspartner.dto.InvestmentResponse;
import fr.formationacademy.scpiinvestpluspartner.entity.ScpiDocument;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.feign.NotificationClient;
import fr.formationacademy.scpiinvestpluspartner.repository.InvestmentRepository;
import fr.formationacademy.scpiinvestpluspartner.repository.ScpiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState.*;
import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.SCPI_PARTNER_RESPONSE_TOPIC;

@Service
@Slf4j
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TemplateEngine templateEngine;
    private final NotificationClient notificationClient;
    private final ScpiRepository scpiRepository;

    public InvestmentService(InvestmentRepository investmentRepository, KafkaTemplate<String, Object> kafkaTemplate, TemplateEngine templateEngine, NotificationClient notificationClient, ScpiRepository scpiRepository) {
        this.investmentRepository = investmentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.templateEngine = templateEngine;
        this.notificationClient = notificationClient;
        this.scpiRepository = scpiRepository;
    }

    public void updateInvestmentStatus(Integer id, InvestmentState status, String rejectReason) {

        investmentRepository.findById(id).ifPresent(investment -> {
            investment.setInvestmentState(status);
            investment.setRejectReason(rejectReason);
            investmentRepository.save(investment);

            ScpiDocument scpi = scpiRepository.findByName(investment.getScpiName()).orElseThrow(
                    () -> new IllegalStateException("SCPI " + investment.getScpiName() + "  not found in the database ")
            );

            log.info("SCpi chargée :  {} ", scpi);
            InvestmentResponse response = InvestmentResponse.builder()
                    .investmentId(id)
                    .investmentState(status)
                    .rejectionReason(rejectReason).build();

            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);

            Context context = new Context();
            if (status == ACCEPTED) {
                context.setVariable("status", status);
                context.setVariable("amount", investment.getAmount());
                context.setVariable("numberPart", investment.getAmount().divide(scpi.getSharePrice(), 2, RoundingMode.UP).intValue());
                context.setVariable("bic", scpi.getBic());
                context.setVariable("iban", scpi.getIban());
                context.setVariable("label", investment.getInvestmentId());
            } else if (status == REJECTED) {
                context.setVariable("status", status);
                context.setVariable("rejectionReason", rejectReason);
                context.setVariable("scpiName", investment.getScpiName());

            }
            log.info("Fin du traitement de la demande  {} ", context);
            log.info("Starting generating template ... ");
            String templateInHtmlFormat = templateEngine.process("investment_template.html", context);
            log.info("Generating template done ...");
            EmailDtoIn emailDtoIn = EmailDtoIn.builder()
                    .to("me.chekini@gmail.com")
                    .from("me.chekini@gmail.com")
                    .subject("Notification test")
                    .body(templateInHtmlFormat)
                    .bodyType("HTML")
                    .build();
            try {
                notificationClient.sendEmail(emailDtoIn);
                log.info("Notification email sent.");
            }catch (Exception e){
                log.error(e.getMessage());
                log.info("Error sending notification");
            }
            investment.setInvestmentState(PENDING_PAYMENT);
            investmentRepository.save(investment);

            response.setInvestmentState(PENDING_PAYMENT);
            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);
            log.info("L'investissement est passé en mode : Pendign payement : {}", investment);


        });


    }

    public void proceedForPayment(Integer label, BigDecimal amount, String iban, String bic) {
        investmentRepository.findById(label).ifPresent(investment -> {
            investment.setInvestmentState(VALIDATED);
            investmentRepository.save(investment);

            InvestmentResponse response = InvestmentResponse.builder()
                    .investmentId(label)
                    .investmentState(VALIDATED)
                    .build();
            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);
        });
    }
}
