package com.NotificationService.NotificationService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    // Exchange name
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Routing keys
    public static final String ASSESSMENT_STARTED_KEY = "notification.assessment.started";
    public static final String ASSESSMENT_SUBMITTED_KEY = "notification.assessment.submitted";
    public static final String ASSESSMENT_EVALUATED_KEY = "notification.assessment.evaluated";
    public static final String ASSESSMENT_REMINDER_KEY = "notification.assessment.reminder";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true); // durable queue
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding assessmentStartedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(ASSESSMENT_STARTED_KEY);
    }

    @Bean
    public Binding assessmentSubmittedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(ASSESSMENT_SUBMITTED_KEY);
    }

    @Bean
    public Binding assessmentEvaluatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(ASSESSMENT_EVALUATED_KEY);
    }

    @Bean
    public Binding assessmentReminderBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(ASSESSMENT_REMINDER_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

