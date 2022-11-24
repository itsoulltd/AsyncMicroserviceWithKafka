package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.tasks.ConsolePrintTask;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/v1")
public class ProductController {

    private static Logger LOG = LoggerFactory.getLogger(ProductController.class.getSimpleName());
    private TaskQueue queue;

    public ProductController(TaskQueue queue) {
        this.queue = queue;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        //Type-1:DispatchTaskInto-KafkaQueue:-
        ConsolePrintTask task = new ConsolePrintTask();
        task.setMessage(new Message().setPayload("ProductService" + message));
        queue.add(task);
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/purchase")
    public ResponseEntity<Response> purchase(@RequestBody SearchQuery purchase) {
        LOG.info("PURCHASE: Message received {} ", purchase.toString());
        //Call RestTemplate: api/payment/v1/checkout
        RestTemplate template = new RestTemplateBuilder()
                .rootUri("http://localhost:8092/api/payment")
                .build();
        purchase.add("order-id").isEqualTo("01928374");
        HttpEntity<SearchQuery> entity = new HttpEntity<>(purchase, new HttpHeaders());
        ResponseEntity<Response> res = template.exchange("/v1/checkout"
                , HttpMethod.POST
                , entity
                , Response.class);
        return new ResponseEntity(res.getBody(), HttpStatus.OK);
    }

}
