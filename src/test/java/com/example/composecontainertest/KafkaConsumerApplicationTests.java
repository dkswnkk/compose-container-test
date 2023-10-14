package com.example.composecontainertest;

import com.example.composecontainertest.service.KafkaConsumerService;
import com.example.composecontainertest.service.KafkaProducerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class KafkaConsumerApplicationTests extends IntegrationTest {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    // 실제 KafkaConsumerService 대신 mock 객체를 생성합니다.
    // mock 객체는 실제 로직을 실행하지 않으며, 호출 여부나 인자 값 등을 확인하는 데 사용됩니다.
    @MockBean
    private KafkaConsumerService kafkaConsumerService;

    // 카프카 메시지 전송 및 수신 테스트
    @Test
    public void kafkaSendAndConsumeTest() {
        String topic = "test-topic";
        String expectValue = "expect-value";

        kafkaProducerService.send(topic, expectValue);

        // ArgumentCaptor: mock 객체에 전달된 인자를 캡처해 후에 그 값을 검증하기 위해 사용합니다.
        var stringCaptor = ArgumentCaptor.forClass(String.class);

        // kafkaConsumerService.process 메소드가 특정 시간 내에 한 번 호출되었는지 확인합니다.
        // 또한, 그 때 어떤 인자로 호출되었는지 stringCaptor를 통해 캡처합니다.
        Mockito.verify(kafkaConsumerService, Mockito.timeout(5000).times(1))
                .process(stringCaptor.capture());

        // 캡처된 인자와 기대값이 일치하는지 검증합니다.
        Assertions.assertEquals(expectValue, stringCaptor.getValue());
    }
}
