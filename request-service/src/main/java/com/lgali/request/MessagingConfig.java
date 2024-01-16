package com.lgali.request;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType.CORRELATED;

@ComponentScan
@Configuration
@Slf4j
public class MessagingConfig {

    @Value("${spring.rabbitmq.template.retry.max-interval}")
    private Integer maxInterval;

    @Value("${spring.rabbitmq.template.retry.initial-interval}")
    private Long initialInterval;

    @Value("${spring.rabbitmq.template.retry.max-attempts}")
    private Integer maxAttemps;

    @Value("${spring.rabbitmq.template.retry.multiplier}")
    private Double multiplier;

    @Bean
    public ConnectionFactory connectionFactory(@Value("${spring.rabbitmq.host}") final String host,
                                               @Value("${spring.rabbitmq.port}") final Integer port,
                                               @Value("${spring.rabbitmq.user}") final String user,
                                               @Value("${spring.rabbitmq.password}") final String password) {

        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host, port);
        cachingConnectionFactory.setUsername(user);
        cachingConnectionFactory.setPassword(password);
        cachingConnectionFactory.setPublisherReturns(true);
        cachingConnectionFactory.setPublisherConfirmType(CORRELATED);
        return cachingConnectionFactory;
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingClass("org.springframework.orm.jpa.JpaTransactionManager")
    public RabbitTransactionManager rabbitTransactionManager(ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }

    @Bean
    @Qualifier("lgaliRabbitTemplate")
    public RabbitTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttemps);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        rabbitTemplate.setRetryTemplate(retryTemplate);

        rabbitTemplate.setConfirmCallback(
          (correlationData, ack, cause) -> {
              log.info(" =====> correlationData = {}", correlationData);
              log.info(" =====> ack = {}", ack);
              log.info(" =====> cause = {}", cause);
          }
        );

        rabbitTemplate.setReturnCallback(
          (message, replyCode, replyText, exchange, routingKey) -> {
              log.info(" =====> message = " + message);
              log.info(" =====> replyCode = " + replyCode);
              log.info(" =====> replyText = " + replyText);
              log.info(" =====> exchange = " + exchange);
              log.info(" =====> routingKey = " + routingKey);
          }
        );

        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory listenerFactory(final ConnectionFactory connectionFactory,
                                                                final SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

}
