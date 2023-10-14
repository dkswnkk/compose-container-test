package com.example.composecontainertest.service;

import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    // 메시지 처리: 현재는 콘솔에 출력
    public void process(String message) {
        System.out.println("processing ... " + message);
    }
}
