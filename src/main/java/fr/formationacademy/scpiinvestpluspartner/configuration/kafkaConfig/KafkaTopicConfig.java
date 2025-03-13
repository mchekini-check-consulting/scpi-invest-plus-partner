package fr.formationacademy.scpiinvestpluspartner.configuration.kafkaConfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.SCPI_PARTNER_TOPIC;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic getTopic() {
        return TopicBuilder.name(SCPI_PARTNER_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
