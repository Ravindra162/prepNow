package com.Submission.SubmissionService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Notification Exchange and Queue
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String ASSESSMENT_STARTED_KEY = "notification.assessment.started";
    public static final String ASSESSMENT_SUBMITTED_KEY = "notification.assessment.submitted";
    public static final String ASSESSMENT_EVALUATED_KEY = "notification.assessment.evaluated";

    // Evaluation Exchange and Queue
    public static final String EVALUATION_EXCHANGE = "evaluation.exchange";
    public static final String EVALUATION_QUEUE = "evaluation.queue";
    public static final String EVALUATION_REQUEST_KEY = "evaluation.request";

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

    // Evaluation Queue Configuration
    @Bean
    public Queue evaluationQueue() {
        return new Queue(EVALUATION_QUEUE, true); // durable queue
    }

    @Bean
    public TopicExchange evaluationExchange() {
        return new TopicExchange(EVALUATION_EXCHANGE);
    }

    @Bean
    public Binding evaluationRequestBinding() {
        return BindingBuilder
                .bind(evaluationQueue())
                .to(evaluationExchange())
                .with(EVALUATION_REQUEST_KEY);
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
