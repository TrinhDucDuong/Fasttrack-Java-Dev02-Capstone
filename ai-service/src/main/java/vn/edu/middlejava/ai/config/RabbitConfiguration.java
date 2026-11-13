package vn.edu.middlejava.ai.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    public static final String MATERIAL_INDEXING_QUEUE = "material.indexing";

    @Bean
    Queue materialIndexingQueue() {
        return new Queue(MATERIAL_INDEXING_QUEUE, true);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
