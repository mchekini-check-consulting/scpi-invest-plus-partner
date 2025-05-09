package fr.formationacademy.scpiinvestpluspartner.configuration.kafka;

import fr.formationacademy.scpiinvestpluspartner.utils.TopicNameProvider;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    private final TopicNameProvider topicNameProvider;

    public KafkaTopicConfig(TopicNameProvider topicNameProvider) {
        this.topicNameProvider = topicNameProvider;
    }

    @Bean
    public NewTopic getTopic() {
        return TopicBuilder.name(topicNameProvider.getScpiInvestRequestTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic getResponseTopic() {
        return TopicBuilder.name(topicNameProvider.getScpiInvestPartnerResponseTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

}
