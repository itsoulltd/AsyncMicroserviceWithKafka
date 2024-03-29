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
    private String orderExeTopic;

    public PaymentController(@Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.order.execute}") String orderExeTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderExeTopic = orderExeTopic;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Response> checkout(@RequestBody SearchQuery checkout) {
        LOG.info("CHECKOUT: Message received {} ", checkout.toString());
        Response response = new Response().setMessage("CHECKOUT: Queued at " + System.currentTimeMillis());
        //Make Some Delay:
        try { Thread.sleep(3000); } catch (InterruptedException e) {}
        //Type-2:DispatchTaskInto-KafkaQueue:-
        kafkaTemplate.send(orderExeTopic, checkout.toString());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @KafkaListener(topics = {"${topic.payment.execute}"}, concurrency = "1")
    public void startListener(@Payload String message, Acknowledgment ack) {
        LOG.info("PAYMENT-EXE-QUEUE: Message received {} ", message);
        //TODO:
        ack.acknowledge();
    }

    @KafkaListener(topics = {"${topic.payment.abort}"}, concurrency = "1")
    public void abortListener(@Payload String message, Acknowledgment ack) {
        LOG.info("PAYMENT-ABORT-QUEUE: Message received {} ", message);
        //TODO:
        ack.acknowledge();
    }

}
