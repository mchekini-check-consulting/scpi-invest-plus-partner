package fr.formationacademy.scpiinvestpluspartner.service;

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

    public String generateHtml(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    //CETTE METHODE POUR JUSTE TESTER LE RESULT DE TEMPLATE
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