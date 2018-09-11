package com.poc.dbqueue.producer;

import com.poc.dbqueue.Constants;
import com.poc.dbqueue.Person;
import com.poc.dbqueue.PersonRepository;
import com.poc.dbqueue.consumer.ApprovingReceiver;
import com.poc.dbqueue.consumer.ProcessingReceiver;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer implements CommandLineRunner {

  Logger logger = LoggerFactory.getLogger(MessageProducer.class);

  private final RabbitTemplate rabbitTemplate;
  private final ProcessingReceiver processingReceiver;
  private final ApprovingReceiver approvingReceiver;

  @Autowired
  private ThreadPoolTaskExecutor executor;

  @Autowired
  private PersonRepository repository;

  public MessageProducer(ProcessingReceiver processingReceiver, ApprovingReceiver approvingReceiver,
      RabbitTemplate rabbitTemplate) {
    this.processingReceiver = processingReceiver;
    this.approvingReceiver = approvingReceiver;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public void run(String... args) throws Exception {

    IntStream.rangeClosed(1, Constants.PROCESS_ITEMS).forEach(i ->
        executor.execute(() -> {
          long start = System.currentTimeMillis();
          logger.debug("Sending message (" + i + ")...");
          Person person = new Person();
          person.setId(""+i);
          person.setFirstName("fName_" + i);
          person.setLastName("lName_" + i);
          String id = repository.save(person).getId();
          rabbitTemplate.convertAndSend(Constants.topicExchangeName, "foo.bar.baz", id);
          long total = System.currentTimeMillis() - start;
          logger.debug("Sending message (" + i + ") finished in " + total);
        })
    );

    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.shutdown();

    processingReceiver.getLatch().await(10, TimeUnit.MINUTES);
    approvingReceiver.getLatch().await(10, TimeUnit.MINUTES);
  }

}