package com.example.loggingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public DirectExchange logsExchange() {
        return new DirectExchange("logs.exchange", true, false);
    }

    @Bean
    public Queue logsQueue() {
        return new Queue("logs.queue", true);
    }

    @Bean
    public Binding logsBinding(Queue logsQueue, DirectExchange logsExchange) {
        // empty routing key "" to match your LoggingService publisher
        return BindingBuilder.bind(logsQueue).to(logsExchange).with("");
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
