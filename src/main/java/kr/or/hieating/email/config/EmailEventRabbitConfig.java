package kr.or.hieating.email.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailEventProperties.class)
public class EmailEventRabbitConfig {

  @Bean
  public TopicExchange emailEventExchange(EmailEventProperties properties) {
    return new TopicExchange(properties.exchange(), true, false);
  }

  @Bean
  public Queue emailEventQueue(EmailEventProperties properties) {
    return new Queue(properties.queue(), true);
  }

  @Bean
  public Binding emailEventBinding(
      Queue emailEventQueue, TopicExchange emailEventExchange, EmailEventProperties properties) {
    return BindingBuilder.bind(emailEventQueue)
        .to(emailEventExchange)
        .with(properties.routingKey());
  }

  @Bean
  public MessageConverter emailEventMessageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      RabbitTemplateConfigurer configurer,
      ConnectionFactory connectionFactory,
      MessageConverter emailEventMessageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate();
    configurer.configure(rabbitTemplate, connectionFactory);
    rabbitTemplate.setMessageConverter(emailEventMessageConverter);
    rabbitTemplate.setMandatory(true);
    return rabbitTemplate;
  }
}
