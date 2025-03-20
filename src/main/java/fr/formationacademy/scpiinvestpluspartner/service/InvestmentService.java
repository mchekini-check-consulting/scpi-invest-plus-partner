package fr.formationacademy.scpiinvestpluspartner.service;

import fr.formationacademy.scpiinvestpluspartner.dto.EmailDtoIn;
import fr.formationacademy.scpiinvestpluspartner.dto.InvestmentResponse;
import fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState;
import fr.formationacademy.scpiinvestpluspartner.feign.NotificationClient;
import fr.formationacademy.scpiinvestpluspartner.repository.InvestmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

import static fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState.PENDING_PAYMENT;
import static fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState.VALIDATED;
import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.SCPI_PARTNER_RESPONSE_TOPIC;

@Service
@Slf4j
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TemplateEngine templateEngine;
    private final NotificationClient notificationClient;

    public InvestmentService(InvestmentRepository investmentRepository, KafkaTemplate<String, Object> kafkaTemplate, TemplateEngine templateEngine, NotificationClient notificationClient) {
        this.investmentRepository = investmentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.templateEngine = templateEngine;
        this.notificationClient = notificationClient;
    }

    public void updateInvestmentStatus(Integer id, InvestmentState status, String rejectReason) {

        investmentRepository.findById(id).ifPresent(investment -> {
            investment.setInvestmentState(status);
            investment.setRejectReason(rejectReason);
            investmentRepository.save(investment);


            InvestmentResponse response = InvestmentResponse.builder()
                    .investmentId(id)
                    .investmentState(status)
                    .rejectionReason(rejectReason).build();

            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);

            Context context = new Context();
            context.setVariable("status", status);
            context.setVariable("amount", investment.getAmount());
            context.setVariable("numberPart", 10); // TODO
            context.setVariable("bic", "ABCDFRPP");
            context.setVariable("iban", "FR76 12345 67890 12345678901 12");
            context.setVariable("label", investment.getInvestmentId());

            log.info("Starting generating template ...");
            String templateInHtmlFormat = templateEngine.process("investment_template.html", context);
            log.info("Generating template done ...");

            EmailDtoIn emailDtoIn = EmailDtoIn.builder()
                    .to("me.chekini@gmail.com")
                    .from("me.chekini@gmail.com")
                    .subject("Notification test")
                    .body(templateInHtmlFormat)
                    .bodyType("HTML")
                    .build();

            notificationClient.sendEmail(emailDtoIn);
            investment.setInvestmentState(PENDING_PAYMENT);
            investmentRepository.save(investment);

            response.setInvestmentState(PENDING_PAYMENT);
            kafkaTemplate.send(SCPI_PARTNER_RESPONSE_TOPIC, response);



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
