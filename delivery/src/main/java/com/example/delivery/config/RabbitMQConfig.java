package com.example.delivery.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange("message.exchange", true, false);
    }

    @Bean
    public DirectExchange logsExchange() {
        return new DirectExchange("logs.exchange", true, false);
    }

    @Bean
    public Queue logsQueue() {
        return new Queue("logs.queue", true);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue("sms", true);
    }

    @Bean
    public Queue whatsappQueue() {
        return new Queue("whatsapp", true);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue("email", true);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, DirectExchange messageExchange) {
        return BindingBuilder.bind(smsQueue).to(messageExchange).with("sms");
    }

    @Bean
    public Binding whatsappBinding(Queue whatsappQueue, DirectExchange messageExchange) {
        return BindingBuilder.bind(whatsappQueue).to(messageExchange).with("whatsapp");
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange messageExchange) {
        return BindingBuilder.bind(emailQueue).to(messageExchange).with("email");
    }

    @Bean
    public Binding logsBinding(Queue logsQueue, DirectExchange logsExchange) {
        return BindingBuilder.bind(logsQueue).to(logsExchange).with("");
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper objectMapper) {
        return new JacksonJsonMessageConverter(objectMapper);
    }

}
