package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskCompletionListener;
import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.tasks.ConsolePrintTask;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.events.Event;
import com.infoworks.lab.rest.models.events.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/v1")
public class ProductController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(ProductController.class.getSimpleName());
    private TaskQueue taskQueue;

    public ProductController(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        Message mac = new Response().setMessage("ProductController: " + message);
        ConsolePrintTask consolePrintTask = new ConsolePrintTask();
        consolePrintTask.setMessage(mac);
        taskQueue.add(consolePrintTask);
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/purchase")
    public ResponseEntity<Response> purchase(@RequestBody Event purchase) {
        //Call RestTemplate: api/payment/v1/checkout
        RestTemplate template = new RestTemplateBuilder()
                .rootUri("http://localhost:8092/api/payment")
                .build();
        HttpEntity<Event> entity = new HttpEntity<>(purchase, new HttpHeaders());
        ResponseEntity<Response> res = template.exchange("/v1/checkout"
                , HttpMethod.POST
                , entity
                , Response.class);
        //
        return new ResponseEntity(res.getBody(), HttpStatus.OK);
    }

    @Override
    public void failed(Message message) {
        System.out.println("RUNNING ON " + Thread.currentThread().getName());
        System.out.println(message.toString());
    }

    @Override
    public void finished(Message message) {
        System.out.println("RUNNING ON " + Thread.currentThread().getName());
        System.out.println(message.toString());
    }
}
