package com.turkcell.customer_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_CUSTOMER_REGISTERED = "customer.registered";
    public static final String TOPIC_CUSTOMER_KYC_APPROVED = "customer.kyc-approved";
    public static final String TOPIC_CUSTOMER_UPDATED = "customer.updated";

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic customerRegisteredTopic() {
        return TopicBuilder.name(TOPIC_CUSTOMER_REGISTERED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic customerKycApprovedTopic() {
        return TopicBuilder.name(TOPIC_CUSTOMER_KYC_APPROVED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic customerUpdatedTopic() {
        return TopicBuilder.name(TOPIC_CUSTOMER_UPDATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
