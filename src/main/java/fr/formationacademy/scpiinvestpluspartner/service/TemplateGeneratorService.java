package fr.formationacademy.scpiinvestpluspartner.service;

import fr.formationacademy.scpiinvestpluspartner.dto.InvestmentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@Service
@Slf4j
public class TemplateGeneratorService {
    private final TemplateEngine templateEngine;

    public TemplateGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateHtml(String templateName, InvestmentResponse response) {
        Context context = new Context();
        log.info("Données envoyées à generateHtml : {}", response);
        context.setVariable("response", response);
        return templateEngine.process(templateName, context);
    }

    public String generateHtml(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    //CETTE METHODE POUR JUSTE TESTER LE RESULT DE TEMPLATE
    public void generateTemplate(String htmlContent) throws Exception {
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException("Le contenu HTML ne peut pas être vide.");
        }String fileName = "investment_" + System.currentTimeMillis() + ".html";
        String os = System.getProperty("os.name").toLowerCase();
        String tempFilePath = System.getProperty("java.io.tmpdir") + fileName;
        Files.writeString(Paths.get(tempFilePath), htmlContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        if (os.contains("win")) {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + tempFilePath);
        } else if (os.contains("mac")) {
            Runtime.getRuntime().exec("open " + tempFilePath);
        } else if (os.contains("nix") || os.contains("nux")) {
            Runtime.getRuntime().exec("xdg-open " + tempFilePath);
        } else {
            throw new UnsupportedOperationException("Système d'exploitation non supporté");
        }
    }
    public void saveAndOpenHtml(String htmlContent, String fileName) throws Exception {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Le nom du fichier ne peut pas être vide.");
        }

        String home = System.getProperty("user.home");
        Path downloadPath = Paths.get(home, "Downloads", fileName + ".html");
        Files.writeString(downloadPath, htmlContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        String filePath = downloadPath.toAbsolutePath().toString();
        String os = System.getProperty("os.name").toLowerCase();
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + filePath);

    }

}