package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.tasks.PurchaseTask;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/v1")
public class ProductController {

    private static Logger LOG = LoggerFactory.getLogger(ProductController.class.getSimpleName());
    private TaskQueue queue;

    public ProductController(TaskQueue queue) {
        this.queue = queue;
    }

    @PostMapping("/purchase/async")
    public ResponseEntity<String> asyncPurchase(@RequestBody SearchQuery purchase){
        LOG.info("ASYNC PURCHASE: Message received {} ", purchase.toString());
        //Adding additional attribute:
        purchase.add("order-id").isEqualTo("01928373");
        //Type-1:Dispatch PurchaseTask Into KafkaQueue:-
        PurchaseTask task = new PurchaseTask(purchase);
        queue.add(task);
        return new ResponseEntity("Async Purchase Has Been Dispatched", HttpStatus.OK);
    }

    @PostMapping("/purchase")
    public ResponseEntity<Response> purchase(@RequestBody SearchQuery purchase) {
        LOG.info("PURCHASE: Message received {} ", purchase.toString());
        //Adding additional attribute:
        purchase.add("order-id").isEqualTo("01928374");
        //Call RestTemplate: api/payment/v1/checkout
        RestTemplate template = new RestTemplateBuilder()
                .rootUri("http://localhost:8092/api/payment")
                .build();
        HttpEntity<SearchQuery> entity = new HttpEntity<>(purchase, new HttpHeaders());
        ResponseEntity<Response> res = template.exchange("/v1/checkout"
                , HttpMethod.POST
                , entity
                , Response.class);
        return new ResponseEntity(res.getBody(), HttpStatus.OK);
    }

}
