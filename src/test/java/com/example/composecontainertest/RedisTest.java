package com.example.composecontainertest;

import com.example.composecontainertest.service.RedisService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisTest extends IntegrationTest {
    @Autowired
    private RedisService redisService;

    @Test
    @DisplayName("Redis Get / Set 테스트")
    public void redisGetSetTest() {
        // given
        String key = "name";
        String expectedValue = "dkswnkk";

        // when
        redisService.set(key, expectedValue);

        // then
        String actualValue = redisService.get(key);
        Assertions.assertEquals(expectedValue, actualValue);
    }
}