package com.infoworks.lab.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.sql.executor.QueryExecutor;
import com.infoworks.tasks.PaymentTask;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.OrderResponse;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskStack;
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

import javax.annotation.Resource;
import java.util.function.BiConsumer;

@RestController
@RequestMapping("/v1")
public class OrderController {

    private static Logger LOG = LoggerFactory.getLogger(OrderController.class.getSimpleName());
    private TaskQueue queue;
    private KafkaTemplate<String, String> kafkaTemplate;
    private String paymentQueue;
    private ObjectMapper mapper;

    public OrderController(@Qualifier("taskDispatchQueue") TaskQueue queue
            , @Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.payment.execute}") String paymentQueue
            , ObjectMapper mapper) {
        this.queue = queue;
        this.queue.onTaskComplete(onTaskCompletion);
        this.kafkaTemplate = kafkaTemplate;
        this.paymentQueue = paymentQueue;
        this.mapper = mapper;
    }

    private BiConsumer<Message, TaskStack.State> onTaskCompletion = (message, state) -> {
        //Order-Flow:
        if (message instanceof OrderResponse) {
            OrderResponse response = (OrderResponse) message;
            if (response.getOptStatus() == OptStatus.CREATE) {
                //paymentService.add(new PaymentTask(response.getOrderID(), response.getMessage()));
                Task paymentTask = new PaymentTask(response.getOrderID(), response.getMessage());
                String jmsMessage = JmsMessageUtil.convert(paymentTask, mapper).toString();
                kafkaTemplate.send(paymentQueue, jmsMessage);
            } else {
                //TODO:
            }
        } else {
            //TODO: When Failed
        }
    };

    /**
     * Example of inject @Scope beans.
     * e.g. @RequestScope bean SQLExecutor to do JDBC-Calls to database.
     */
    @Resource(name = "executor")
    private QueryExecutor executor;

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        return new ResponseEntity(message, HttpStatus.OK);
    }

}
