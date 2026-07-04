package com.infoworks.lab.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.lab.domain.tasks.PurchaseTask;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.sql.query.pagination.SearchQuery;
import com.infoworks.tasks.OrderTask;
import com.infoworks.tasks.Task;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/v1")
public class ProductController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(ProductController.class.getSimpleName());
    private TaskQueue queue;
    private KafkaTemplate<String, String> kafkaTemplate;
    private String orderQueue;
    private ObjectMapper mapper;

    public ProductController(@Qualifier("taskDispatchQueue") TaskQueue queue
            , @Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate
            , @Value("${topic.order.execute}") String orderQueue
            , ObjectMapper mapper) {
        this.queue = queue;
        //Attaching Queue with task-completion-listener:- when a task get executed by the consumer.
        this.queue.onTaskComplete(this);
        this.kafkaTemplate = kafkaTemplate;
        this.orderQueue = orderQueue;
        this.mapper = mapper;
    }

    @Override
    public void failed(Message message) {
        if(message != null) LOG.error("Product-Consumer Exe Failed: {}", message);
        //TODO:
    }

    @Override
    public void finished(Message message) {
        if(message != null) LOG.info("Product-Consumer Exe Successful: {}", message);
        //TODO:
    }

    @GetMapping("/inventory/{product_id}/itemCount")
    public ResponseEntity<Map> inventoryItemCount(@PathVariable String product_id) {
        //TODO:
        Map<String, Object> data = new HashMap<>();
        data.put("itemCount", new Random().nextInt(10));
        return ResponseEntity.ok(data);
    }

    @PostMapping("/purchase/{product_id}")
    public ResponseEntity<Response> purchaseItem(@PathVariable String product_id
            , @RequestBody SearchQuery payload) {

        LOG.info("PURCHASE: Product_Id {}, Message received {} ", product_id, payload.toString());
        Response response = new Response().setStatus(404);

        //Create an OrderTask for product_id:
        Random random = new Random(); //For the simulation of success & failure.
        String productInfo = Optional.ofNullable(payload.get("product_name")).orElse("").toString();
        if (productInfo.isEmpty())
            return ResponseEntity.status(400).body(response.setError("product_name did not set in the payload."));

        Task orderTask = new OrderTask(product_id, productInfo, random.nextBoolean());
        String jmsMessage = JmsMessageUtil.convert(orderTask, mapper).toString();
        kafkaTemplate.send(orderQueue, jmsMessage);
        //...
        response.setStatus(200)
                .setMessage(String.format("Purchase for \"%s\" has been dispatched.", product_id));
        return new ResponseEntity(response, HttpStatus.OK);
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
