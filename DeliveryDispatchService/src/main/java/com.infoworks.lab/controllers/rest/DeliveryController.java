package com.infoworks.lab.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.tasks.PaymentCancelTask;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.ShipmentResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class DeliveryController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(DeliveryController.class.getSimpleName());
    private TaskQueue queue;
    private KafkaTemplate<String, String> kafkaTemplate;
    private String paymentQueue;
    private ObjectMapper mapper;

    public DeliveryController(@Qualifier("taskDispatchQueue") TaskQueue queue
            , @Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.payment.execute}") String paymentQueue
            , ObjectMapper mapper) {
        this.queue = queue;
        //Attaching Queue with task-completion-listener:- when a task get executed by the consumer.
        this.queue.onTaskComplete(this);
        this.kafkaTemplate = kafkaTemplate;
        this.paymentQueue = paymentQueue;
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
        //Shipping-Flow:
        if (message instanceof ShipmentResponse) {
            ShipmentResponse response = (ShipmentResponse) message;
            if (response.getOptStatus() == OptStatus.CREATE) {
                LOG.info("\uD83D\uDE0E " + "[order-id: " + response.getOrderID() + "] "
                        + "==>|| Shipping Complete For OrderID:" + response.getOrderID() + " (" + response.getMessage() + ") ||<==");
            } else if(response.getOptStatus() == OptStatus.CANCEL) {
                //paymentService.add(new PaymentCancelTask(response.getOrderID(), response.getPaymentID(), response.getMessage()));
                Task paymentCancelTask = new PaymentCancelTask(response.getOrderID(), response.getPaymentID(), response.getMessage());
                String jmsMessage = JmsMessageUtil.convert(paymentCancelTask, mapper).toString();
                kafkaTemplate.send(paymentQueue, jmsMessage);
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

}
