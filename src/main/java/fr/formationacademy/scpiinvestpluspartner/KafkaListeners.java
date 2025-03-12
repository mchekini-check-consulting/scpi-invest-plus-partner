package fr.formationacademy.scpiinvestpluspartner;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    @KafkaListener(topics = "topic1", groupId = "grp_Id")
    void listener(String data){
        System.out.println("Listening to the data: " + data + "! ");
    }
}
