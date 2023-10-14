package com.example.composecontainertest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 주어진 토픽으로 메시지 전송
    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
