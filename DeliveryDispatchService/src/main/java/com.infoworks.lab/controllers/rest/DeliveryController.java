package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v1")
public class DeliveryController {

    private static Logger LOG = LoggerFactory.getLogger(DeliveryController.class.getSimpleName());
    private KafkaTemplate<String, String> kafkaTemplate;

    public DeliveryController(@Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @KafkaListener(topics = {"${topic.execute}"}, concurrency = "1")
    public void startListener(@Payload String message, Acknowledgment ack) {
        //Retrieve the message content
        LOG.info("DELIVERY-EXE-QUEUE: Message received {} ", message);
        try {
            //Dispatch Into Queue:
            SearchQuery query = Message.unmarshal(SearchQuery.class, message);
            query.add("lat").isEqualTo(0.92137)
                    .and("lon").isEqualTo(9.00)
                    .and("customer-name").isEqualTo("Dr. Cooper")
                    .and("address").isEqualTo("House#911, Rode#12B, Tikatuli Nakhalpara Taltola");
            LOG.info("Dispatch Delivery: {}", query.toString());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = {"${topic.abort}"}, concurrency = "1")
    public void abortListener(@Payload String message, Acknowledgment ack) {
        //Retrieve the message content
        LOG.info("DELIVERY-ABORT-QUEUE: Message received {} ", message);
        //TODO:
        ack.acknowledge();
    }

}
