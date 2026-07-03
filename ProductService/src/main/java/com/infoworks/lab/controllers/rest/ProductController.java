package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.domain.tasks.PurchaseTask;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.sql.query.pagination.SearchQuery;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskCompletionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ProductController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(ProductController.class.getSimpleName());
    private TaskQueue queue;
    private KafkaTemplate<String, String> kafkaTemplate;
    private String orderQueue;

    public ProductController(@Qualifier("taskDispatchQueue") TaskQueue queue
            , @Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.order.execute}") String orderQueue) {
        this.queue = queue;
        //Attaching Queue with task-completion-listener:- when a task get executed by the consumer.
        this.queue.onTaskComplete(this);
        this.kafkaTemplate = kafkaTemplate;
        this.orderQueue = orderQueue;
    }

    @Override
    public void failed(Message message) {
        if(message != null) LOG.error("Product-Consumer Exe Failed: {}", message);
    }

    @Override
    public void finished(Message message) {
        if(message != null) LOG.info("Product-Consumer Exe Successful: {}", message);
        //TODO: We can dispatch any other work-flow: e.g. Saga or Transactional-Outbox
        //...
    }

    /** Legacy code */

    @PostMapping("/purchase/async")
    public ResponseEntity<Response> asyncPurchase(@RequestBody SearchQuery purchase){
        LOG.info("ASYNC PURCHASE: Message received {} ", purchase.toString());
        //Adding additional attribute:
        purchase.add("order-id").isEqualTo("01928373")
                .and("purchase_task").isEqualTo(System.currentTimeMillis());
        //Type-1:Dispatch PurchaseTask Into KafkaQueue:-
        PurchaseTask task = new PurchaseTask(purchase);
        queue.add(task);
        Response response = new Response().setStatus(200).setMessage("Async Purchase Has Been Dispatched");
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/purchase")
    public ResponseEntity<Response> purchase(@RequestBody SearchQuery purchase) {
        LOG.info("PURCHASE: Message received {} ", purchase.toString());
        //Adding additional attribute:
        purchase.add("order-id").isEqualTo("01928374");
        //Rest-api Call:
        PurchaseTask task = new PurchaseTask(purchase);
        Response response = task.execute(null);
        LOG.info("Product-RestApi Exe Successful: {}", response.toString());
        return new ResponseEntity(response, HttpStatus.OK);
    }

}
