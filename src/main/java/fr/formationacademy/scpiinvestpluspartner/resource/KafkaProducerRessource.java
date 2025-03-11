package fr.formationacademy.scpiinvestpluspartner.resource;

import fr.formationacademy.scpiinvestpluspartner.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demande")
public class KafkaProducerRessource {
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public KafkaProducerRessource(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String message) {
        kafkaProducerService.send("scpi-partner-topic", message);
        return ResponseEntity.ok("Message envoyé: " + message);
    }
}
