package vn.edu.middlejava.ai;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RabbitMqIntegrationTest {
    @Container
    private static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management")
            .withUser("study", "study", java.util.Set.of("administrator"))
            .withPermission("/", "study", ".*", ".*", ".*");

    @Test
    void publishesAndReceivesAMessageFromRabbitMq() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(RABBIT.getHost(), RABBIT.getAmqpPort());
        connectionFactory.setUsername("study");
        connectionFactory.setPassword("study");
        try {
            RabbitAdmin admin = new RabbitAdmin(connectionFactory);
            Queue queue = new Queue("study-coach.integration", false);
            admin.declareQueue(queue);
            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

            rabbitTemplate.convertAndSend(queue.getName(), "material-ready");

            assertThat(rabbitTemplate.receiveAndConvert(queue.getName())).isEqualTo("material-ready");
        } finally {
            connectionFactory.destroy();
        }
    }
}
