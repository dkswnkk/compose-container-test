package com.example.composecontainertest;

import com.redis.testcontainers.RedisContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@Transactional
@ContextConfiguration(initializers = IntegrationTest.IntegrationTestInitializer.class)
public class IntegrationTest {

    private static DockerComposeContainer rdbms;
    private static RedisContainer redis;

    @BeforeAll
    public static void setupContainers() {
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
        redis = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("6"));

        rdbms.start();
        redis.start();
    }

    static class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            Map<String, String> properties = new HashMap<>();

            setDatabaseProperties(properties);
            setRedisProperties(properties);

            TestPropertyValues.of(properties).applyTo(applicationContext);
        }

        private void setDatabaseProperties(Map<String, String> properties) {
            String rdbmsHost = rdbms.getServiceHost("local-db", 3306);
            int rdbmsPort = rdbms.getServicePort("local-db", 3306);

            properties.put("spring.datasource.url", "jdbc:mysql://" + rdbmsHost + ":" + rdbmsPort + "/container");
        }

        private void setRedisProperties(Map<String, String> properties) {
            String redisHost = redis.getHost();
            Integer redisPort = redis.getFirstMappedPort();

            properties.put("spring.redis.host", redisHost);
            properties.put("spring.redis.port", redisPort.toString());
        }
    }
}
