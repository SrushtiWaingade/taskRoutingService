package com.example.taskrouter.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange messageExchange(@Value("${rabbitmq.exchange.message:message.exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public DirectExchange logsExchange(@Value("${rabbitmq.exchange.logs:logs.exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue emailDeliveryQueue() {
        return new Queue("email", true);
    }

    @Bean
    public Queue smsDeliveryQueue() {
        return new Queue("sms", true);
    }

    @Bean
    public Queue whatsappDeliveryQueue() {
        return new Queue("whatsapp", true);
    }

    @Bean
    public Queue commonDeliveryQueue() {
        return new Queue("common.queue", true);
    }

    @Bean
    public Queue logsQueue() {
        return new Queue("logs.queue", true);
    }

    @Bean
    public Binding commonDeliveryBinding(@Qualifier("commonDeliveryQueue") Queue commmonDeliveryQueue,
                                   DirectExchange messageExchange) {
        return BindingBuilder.bind(commmonDeliveryQueue).to(messageExchange).with("");
    }

    @Bean
    public Binding logsBinding(@Qualifier("logsQueue") Queue logsQueue,
                               DirectExchange logsExchange) {
        return BindingBuilder.bind(logsQueue).to(logsExchange).with("");
    }

    @Bean
    public Binding emailBinding(@Qualifier("emailDeliveryQueue") Queue emailDeliveryQueue,
                                DirectExchange messageExchange) {
        return BindingBuilder.bind(emailDeliveryQueue).to(messageExchange).with("email");
    }

    @Bean
    public Binding smsBinding(@Qualifier("smsDeliveryQueue") Queue smsDeliveryQueue,
                              DirectExchange messageExchange) {
        return BindingBuilder.bind(smsDeliveryQueue).to(messageExchange).with("sms");
    }

    @Bean
    public Binding whatsappBinding(@Qualifier("whatsappDeliveryQueue") Queue whatsappDeliveryQueue,
                                   DirectExchange messageExchange) {
        return BindingBuilder.bind(whatsappDeliveryQueue).to(messageExchange).with("whatsapp");
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper objectMapper) {
        return new JacksonJsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
