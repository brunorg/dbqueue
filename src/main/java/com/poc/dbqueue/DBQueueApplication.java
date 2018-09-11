package com.poc.dbqueue;

import com.poc.dbqueue.consumer.ApprovingReceiver;
import com.poc.dbqueue.consumer.ProcessingReceiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@SpringBootApplication
public class DBQueueApplication {

  @Bean
  Queue queue() {
    return new Queue(Constants.queueName, false);
  }

  @Bean
  Queue queueAprove() {
    return new Queue(Constants.queueNameAprove, false);
  }

  @Bean
  TopicExchange exchange() {
    return new TopicExchange(Constants.topicExchangeName);
  }

  @Bean
  Binding binding(Queue queue, TopicExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with("foo.bar.#");
  }

  @Bean
  Binding bindingAprove(Queue queueAprove, TopicExchange exchange) {
    return BindingBuilder.bind(queueAprove).to(exchange).with("foo.barApprove.#");
  }

  @Bean
  SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
      MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(Constants.queueName);
    container.setMessageListener(listenerAdapter);
    container.setConcurrency("5-10");
    return container;
  }

  @Bean
  SimpleMessageListenerContainer containerAprove(ConnectionFactory connectionFactory,
      MessageListenerAdapter listenerAdapterAprove) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(Constants.queueNameAprove);
    container.setMessageListener(listenerAdapterAprove);
    container.setConcurrency("5-10");
    return container;
  }

  @Bean
  MessageListenerAdapter listenerAdapter(ProcessingReceiver processingReceiver) {
    return new MessageListenerAdapter(processingReceiver, "receiveMessage");
  }

  @Bean
  MessageListenerAdapter listenerAdapterAprove(ApprovingReceiver approvingReceiver) {
    return new MessageListenerAdapter(approvingReceiver, "receiveMessage");
  }


  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(50);
    executor.setMaxPoolSize(100);
    executor.setQueueCapacity(Constants.PROCESS_ITEMS);
    executor.setThreadNamePrefix("sched-pool-");
    executor.initialize();

    return executor;

  }


  public static void main(String[] args) {
    SpringApplication.run(DBQueueApplication.class, args).close();
  }

}
