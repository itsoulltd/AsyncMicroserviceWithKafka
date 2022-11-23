package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskCompletionListener;
import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.tasks.ConsolePrintTask;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class OrderController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(OrderController.class.getSimpleName());
    private TaskQueue taskQueue;
    private TaskQueue deliveryQueue;

    public OrderController(TaskQueue taskQueue
            , @Qualifier("deliveryDispatchQueue") TaskQueue deliveryQueue) {
        this.taskQueue = taskQueue;
        this.deliveryQueue = deliveryQueue;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        Message mac = new Response().setMessage("OrderController: " + message);
        ConsolePrintTask consolePrintTask = new ConsolePrintTask();
        consolePrintTask.setMessage(mac);
        taskQueue.add(consolePrintTask);
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/delivery")
    public ResponseEntity<Response> dispatchDelivery(@RequestBody Event checkout) {
        Response response = (Response) new Response().setEvent(checkout);
        ConsolePrintTask console = new ConsolePrintTask();
        console.setMessage(response);
        deliveryQueue.add(console);
        return new ResponseEntity(response, HttpStatus.OK);
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
