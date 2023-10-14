package com.example.composecontainertest.config;

import com.example.composecontainertest.service.KafkaConsumerService;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConsumerApplication {
    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    // 카프카 토픽 설정
    @Bean
    public NewTopic topic() {
        return TopicBuilder.name("test-topic").build();
    }

    // 카프카 리스너: 메시지를 받으면 처리 서비스를 호출
    @KafkaListener(id = "test-id", topics = "test-topic")
    public void listen(String message) {
        kafkaConsumerService.process(message);
    }
}
