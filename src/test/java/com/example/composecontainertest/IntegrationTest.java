package com.example.composecontainertest;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 통합 테스트를 위한 기본 클래스입니다.
 * Docker 환경을 설정하고 테스트를 위한 애플리케이션 컨텍스트를 구성합니다.
 */
@Ignore
@SpringBootTest
@Transactional
@ContextConfiguration(initializers = IntegrationTest.IntegrationTestInitializer.class)
public class IntegrationTest {

    // RDBMS를 위한 DockerComposeContainer의 싱글턴 인스턴스입니다.
    private static final DockerComposeContainer<?> RDBMS = createRDBMSContainer();

    /**
     * RDBMS를 위한 Docker Compose 컨테이너를 생성하고 시작합니다.
     * @return DockerComposeContainer 인스턴스.
     */
    private static DockerComposeContainer<?> createRDBMSContainer() {
        DockerComposeContainer<?> container = new DockerComposeContainer<>(new File("infra/test/docker-compose.yaml"))
                .withExposedService("local-db", 3306, createWaitStrategy(".*ready for connections.*"))
                .withExposedService("local-db-migrate", 0, createWaitStrategy("(.*Successfully applied.*)|(.*Successfully validated.*)"));
        container.start();
        return container;
    }

    /**
     * Docker 서비스를 위한 대기 전략을 생성합니다.
     * @param logMessagePattern 대기할 로그 메시지 패턴.
     * @return 대기 전략.
     */
    private static WaitStrategy createWaitStrategy(String logMessagePattern) {
        return Wait.forLogMessage(logMessagePattern, 1).withStartupTimeout(Duration.ofSeconds(300));
    }


    /**
     * 스프링 애플리케이션 컨텍스트의 초기화기입니다.
     * RDBMS 컨테이너 세부 정보를 기반으로 데이터 소스 속성을 설정합니다.
     */
    static class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            Map<String, String> properties = createDataSourceProperties();
            TestPropertyValues.of(properties).applyTo(applicationContext);
        }

        /**
         * RDBMS 컨테이너 세부 정보를 기반으로 데이터 소스 속성을 생성합니다.
         * @return 데이터 소스 속성 맵.
         */
        private Map<String, String> createDataSourceProperties() {
            Map<String, String> properties = new HashMap<>();
            String rdbmsHost = RDBMS.getServiceHost("local-db", 3306);
            int rdbmsPort = RDBMS.getServicePort("local-db", 3306);
            properties.put("spring.datasource.url", "jdbc:mysql://" + rdbmsHost + ":" + rdbmsPort + "/container");
            return properties;
        }
    }
}
