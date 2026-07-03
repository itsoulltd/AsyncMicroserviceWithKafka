package com.infoworks.lab.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.sql.query.pagination.SearchQuery;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.PaymentResponse;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskCompletionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class PaymentController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(PaymentController.class.getSimpleName());
    private TaskQueue queue;
    private KafkaTemplate<String, String> kafkaTemplate;
    private String deliveryQueue;
    private String orderQueue;
    private ObjectMapper mapper;

    public PaymentController(@Qualifier("taskDispatchQueue") TaskQueue queue
            , @Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.delivery.execute}") String deliveryQueue
            , @Value("${topic.order.execute}") String orderQueue
            , ObjectMapper mapper) {
        this.queue = queue;
        //Attaching Queue with task-completion-listener:- when a task get executed by the consumer.
        this.queue.onTaskComplete(this);
        this.kafkaTemplate = kafkaTemplate;
        this.deliveryQueue = deliveryQueue;
        this.orderQueue = orderQueue;
        this.mapper = mapper;
    }

    @Override
    public void failed(Message message) {
        if(message != null) LOG.error("Order-Consumer Exe Failed: {}", message);
        //TODO:
    }

    @Override
    public void finished(Message message) {
        if (message != null) LOG.info("Order-Consumer Exe Successful: {}", message);
        //TODO:
        //Payment-Flow:
        if (message instanceof PaymentResponse) {
            PaymentResponse response = (PaymentResponse) message;
            if (response.getOptStatus() == OptStatus.CREATE) {
                //shippingService.add(new ShipmentTask(response.getOrderID(), response.getPaymentID(), response.getMessage()));
                //TODO
            } else if (response.getOptStatus() == OptStatus.CANCEL) {
                //orderService.add(new OrderCancelTask(response.getOrderID(), response.getMessage()));
                //TODO
            } else {
                //TODO
            }
        } else {
            //TODO: When Failed
        }
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
        kafkaTemplate.send(deliveryQueue, checkout.toString());
        return new ResponseEntity(response, HttpStatus.OK);
    }

}
