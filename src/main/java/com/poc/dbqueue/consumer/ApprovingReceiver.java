package com.poc.dbqueue.consumer;

import com.poc.dbqueue.Constants;
import com.poc.dbqueue.Person;
import com.poc.dbqueue.Person.Status;
import com.poc.dbqueue.PersonRepository;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApprovingReceiver {

    Logger logger = LoggerFactory.getLogger(ApprovingReceiver.class);

    @Autowired
    private PersonRepository repository;

    private CountDownLatch latch = new CountDownLatch(Constants.PROCESS_ITEMS);

    public void receiveMessage(String message) {
        long start = System.currentTimeMillis();
        logger.debug("ApprovingReceived <" + message + ">");
        Optional<Person> person = repository.findById(message);
        person.ifPresent(p -> {
            p.setStatus(Status.APPROVED);
            repository.save(p);
        });
        long total = System.currentTimeMillis() - start;
        logger.debug("ApprovingReceived <" + message + "> finished in " + total);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}