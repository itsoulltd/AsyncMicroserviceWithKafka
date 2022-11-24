package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class PaymentController {

    private static Logger LOG = LoggerFactory.getLogger(PaymentController.class.getSimpleName());
    private KafkaTemplate<String, String> kafkaTemplate;
    private String orderQueue;
    private String orderAbortQueue;

    public PaymentController(@Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.order.execute}") String orderQueue
            , @Value("${topic.order.abort}") String orderAbortQueue ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderQueue = orderQueue;
        this.orderAbortQueue = orderAbortQueue;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Response> checkout(@RequestBody SearchQuery purchase) {
        Response response = (Response) new Response().setMessage("Action Queued!");
        //Make decision based on message
        kafkaTemplate.send(orderQueue, purchase.toString());
        //or kafkaTemplate.send(orderAbortQueue, "Alert!!!");
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @KafkaListener(topics = {"${topic.execute}"}, concurrency = "1")
    public void startListener(@Payload String message, Acknowledgment ack) {
        //Retrieve the message content
        LOG.info("EXE-QUEUE: Message received {} ", message);
        //TODO:
        ack.acknowledge();
    }

    @KafkaListener(topics = {"${topic.abort}"}, concurrency = "1")
    public void abortListener(@Payload String message, Acknowledgment ack) {
        //Retrieve the message content
        LOG.info("ABORT-QUEUE: Message received {} ", message);
        //TODO:
        ack.acknowledge();
    }

}
