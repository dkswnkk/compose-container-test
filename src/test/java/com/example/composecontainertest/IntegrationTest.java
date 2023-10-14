package com.example.composecontainertest;

import com.redis.testcontainers.RedisContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Disabled
@SpringBootTest
@Transactional
@ContextConfiguration(initializers = IntegrationTest.IntegrationTestInitializer.class)
public class IntegrationTest {

    // 각 컨테이너들의 인스턴스를 정의합니다.
    private static DockerComposeContainer rdbms;
    private static RedisContainer redis;
    private static KafkaContainer kafka;

    // 모든 테스트 전에 Docker 컨테이너를 시작하는 설정입니다.
    @BeforeAll
    public static void setupContainers() {
        // RDBMS 관련 설정
        rdbms = new DockerComposeContainer(new File("infra/test/docker-compose.yaml"))
                .withExposedService(
                        "local-db",
                        3306,
                        Wait.forLogMessage(".*ready for connections.*", 1)
                                .withStartupTimeout(Duration.ofSeconds(300))
                )
                .withExposedService(
                        "local-db-migrate",
                        0,
                        Wait.forLogMessage("(.*Successfully applied.*)|(.*Successfully validated.*)", 1)
                                .withStartupTimeout(Duration.ofSeconds(300))
                );

        // Redis 관련 설정
        redis = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("6"));

        // Kafka 관련 설정
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

        // 컨테이너 시작
        kafka.start();
        rdbms.start();
        redis.start();
    }

    static class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            Map<String, String> properties = new HashMap<>();

            // 각 서비스의 연결 정보를 설정에 추가
            setDatabaseProperties(properties);
            setRedisProperties(properties);
            setKafkaProperties(properties);

            // 애플리케이션 컨텍스트에 속성값 적용
            TestPropertyValues.of(properties).applyTo(applicationContext);
        }

        // Kafka 연결 정보 설정
        private void setKafkaProperties(Map<String, String> properties) {
            properties.put("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
        }

        // 데이터베이스 연결 정보 설정
        private void setDatabaseProperties(Map<String, String> properties) {
            String rdbmsHost = rdbms.getServiceHost("local-db", 3306);
            int rdbmsPort = rdbms.getServicePort("local-db", 3306);
            properties.put("spring.datasource.url", "jdbc:mysql://" + rdbmsHost + ":" + rdbmsPort + "/container");
        }

        // Redis 연결 정보 설정
        private void setRedisProperties(Map<String, String> properties) {
            String redisHost = redis.getHost();
            Integer redisPort = redis.getFirstMappedPort();
            properties.put("spring.redis.host", redisHost);
            properties.put("spring.redis.port", redisPort.toString());
        }
    }
}
