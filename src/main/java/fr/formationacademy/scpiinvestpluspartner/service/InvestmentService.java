package fr.formationacademy.scpiinvestpluspartner.service;

import fr.formationacademy.scpiinvestpluspartner.dto.EmailDtoIn;
import fr.formationacademy.scpiinvestpluspartner.dto.InvestmentResponse;
import fr.formationacademy.scpiinvestpluspartner.entity.ScpiDocument;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.feign.NotificationClient;
import fr.formationacademy.scpiinvestpluspartner.repository.InvestmentRepository;
import fr.formationacademy.scpiinvestpluspartner.repository.ScpiRepository;
import fr.formationacademy.scpiinvestpluspartner.utils.TopicNameProvider;
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
    private final TopicNameProvider topicNameProvider;

    public InvestmentService(InvestmentRepository investmentRepository, KafkaTemplate<String, Object> kafkaTemplate,
                             TemplateEngine templateEngine, NotificationClient notificationClient,
                             ScpiRepository scpiRepository, TopicNameProvider topicNameProvider) {
        this.investmentRepository = investmentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.templateEngine = templateEngine;
        this.notificationClient = notificationClient;
        this.scpiRepository = scpiRepository;
        this.topicNameProvider = topicNameProvider;
    }

    public void updateInvestmentStatus(Integer id, InvestmentState status, String rejectReason) {

        investmentRepository.findById(id).ifPresent(investment -> {
            investment.setInvestmentState(status);
            investment.setRejectReason(rejectReason);
            investmentRepository.save(investment);

            ScpiDocument scpi = scpiRepository.findByName(investment.getScpiName()).orElseThrow(
                    () -> new IllegalStateException("SCPI " + investment.getScpiName() + "  not found in the database ")
            );

            log.info("LOADED SCPI :  {} ", scpi);
            InvestmentResponse response = InvestmentResponse.builder()
                    .investmentId(id)
                    .investmentState(status)
                    .rejectionReason(rejectReason).build();

            kafkaTemplate.send(topicNameProvider.getScpiInvestPartnerResponseTopic(), response);

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

            if(status == ACCEPTED) {
                investment.setInvestmentState(PENDING_PAYMENT);
                log.info("[updateInvestmentStatus] ADD the investment in mongon as PENDING_PAYMENT state ");
                investmentRepository.save(investment);
                response.setInvestmentState(PENDING_PAYMENT);
                kafkaTemplate.send(topicNameProvider.getScpiInvestPartnerResponseTopic(), response); // PARTNER RESPONSE
                log.info("[updateInvestmentStatus]  L'investissement est passé en mode : Pendign payement : {}", investment);
            }


        });


    }

    public void proceedForPayment(Integer label, BigDecimal amount, String iban, String bic) {
        log.info("[proceedForPayment] L'investissement numero {} est en cours de validation de payement", label);
        investmentRepository.findById(label).ifPresent(investment -> {
            investment.setInvestmentState(VALIDATED);
            investmentRepository.save(investment);
            log.info("[proceedForPayment] L'invsitissement a été payé, voici son etat actuel {} ", investment);
            InvestmentResponse response = InvestmentResponse.builder()
                    .investmentId(label)
                    .investmentState(VALIDATED)
                    .build();
            log.info("[proceedForPayment] Réponse du partener vers api {}", response);
            kafkaTemplate.send(topicNameProvider.getScpiInvestPartnerResponseTopic(), response);
        });
    }
}
