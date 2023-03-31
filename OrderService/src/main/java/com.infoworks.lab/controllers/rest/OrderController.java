package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.rest.models.Message;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v1")
public class OrderController {

    private static Logger LOG = LoggerFactory.getLogger(OrderController.class.getSimpleName());
    private KafkaTemplate<String, String> kafkaTemplate;
    private String deliveryExeTopic;

    public OrderController(@Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.delivery.execute}") String deliveryExeTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.deliveryExeTopic = deliveryExeTopic;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @KafkaListener(topics = {"${topic.order.execute}"}, concurrency = "1")
    public void startListener(@Payload String message, Acknowledgment ack) {
        LOG.info("ORDER-EXE-QUEUE: Message received {} ", message);
        try {
            //Type-2:DispatchTaskInto-KafkaQueue:-
            SearchQuery query = Message.unmarshal(SearchQuery.class, message);
            query.add("delivery-id").isEqualTo("92137")
                    .and("status").isEqualTo("SUCCESS");
            kafkaTemplate.send(deliveryExeTopic, query.toString());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = {"${topic.order.abort}"}, concurrency = "1")
    public void abortListener(@Payload String message, Acknowledgment ack) {
        LOG.info("ORDER-ABORT-QUEUE: Message received {} ", message);
        //TODO:
        ack.acknowledge();
    }

}
