package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.tasks.PurchaseTask;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ProductController {

    private static Logger LOG = LoggerFactory.getLogger(ProductController.class.getSimpleName());
    private TaskQueue queue;

    public ProductController(TaskQueue queue) {
        this.queue = queue;
    }

    @PostMapping("/purchase/async")
    public ResponseEntity<Response> asyncPurchase(@RequestBody SearchQuery purchase){
        LOG.info("ASYNC PURCHASE: Message received {} ", purchase.toString());
        //Adding additional attribute:
        purchase.add("order-id").isEqualTo("01928373");
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
        return new ResponseEntity(response, HttpStatus.OK);
    }

}
