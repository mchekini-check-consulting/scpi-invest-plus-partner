package fr.formationacademy.scpiinvestpluspartner.configuration.kafka;

import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.utils.TopicNameProvider;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.SCPI_PARTNER_GROUP;

@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    public String bootstrapServers;

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final TopicNameProvider topicNameProvider;

    public KafkaConsumerConfig(KafkaTemplate<Object, Object> kafkaTemplate, TopicNameProvider topicNameProvider) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicNameProvider = topicNameProvider;
    }

    @Bean
    public ConsumerFactory<String, ScpiRequestDto> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, topicNameProvider.getGroupTopic());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "fr.formationacademy.scpiinvestpluspartner.dto");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(ScpiRequestDto.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ScpiRequestDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ScpiRequestDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(1000L, 2));
    }
}
