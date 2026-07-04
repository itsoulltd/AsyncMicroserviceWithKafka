package com.infoworks.lab.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.sql.query.pagination.SearchQuery;
import com.infoworks.tasks.OrderCancelTask;
import com.infoworks.tasks.ShipmentTask;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.PaymentResponse;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskCompletionListener;
import com.infoworks.utils.JmsMessageUtil;
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
        //if(message != null) LOG.error("Payment-Consumer Exe Failed: {}", message);
        //Payment-Flow: When Failed
        if (message instanceof PaymentResponse) {
            PaymentResponse response = (PaymentResponse) message;
            if (response.getOptStatus() == OptStatus.CANCEL) {
                //orderService.add(new OrderCancelTask(response.getOrderID(), response.getMessage()));
                Task orderCancelTask = new OrderCancelTask(response.getOrderID(), response.getMessage());
                String jmsMessage = JmsMessageUtil.convert(orderCancelTask, mapper).toString();
                kafkaTemplate.send(orderQueue, jmsMessage);
            }
        }
    }

    @Override
    public void finished(Message message) {
        //if (message != null) LOG.info("Payment-Consumer Exe Successful: {}", message);
        //Payment-Flow: When Succeed
        if (message instanceof PaymentResponse) {
            PaymentResponse response = (PaymentResponse) message;
            if (response.getOptStatus() == OptStatus.CREATE) {
                //shippingService.add(new ShipmentTask(response.getOrderID(), response.getPaymentID(), response.getMessage()));
                Task shippingTask = new ShipmentTask(response.getOrderID(), response.getPaymentID(), response.getMessage());
                String jmsMessage = JmsMessageUtil.convert(shippingTask, mapper).toString();
                kafkaTemplate.send(deliveryQueue, jmsMessage);
            } else if (response.getOptStatus() == OptStatus.CANCEL) {
                //orderService.add(new OrderCancelTask(response.getOrderID(), response.getMessage()));
                Task orderCancelTask = new OrderCancelTask(response.getOrderID(), response.getMessage());
                String jmsMessage = JmsMessageUtil.convert(orderCancelTask, mapper).toString();
                kafkaTemplate.send(orderQueue, jmsMessage);
            } else {
                //TODO
            }
        }
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        return new ResponseEntity(message, HttpStatus.OK);
    }

    /** Legacy Code */

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
