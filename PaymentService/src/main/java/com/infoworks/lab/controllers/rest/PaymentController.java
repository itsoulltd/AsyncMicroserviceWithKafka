package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskCompletionListener;
import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.beans.tasks.nuts.SimpleTask;
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
public class PaymentController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(PaymentController.class.getSimpleName());
    private TaskQueue taskQueue;
    private TaskQueue orderQueue;

    public PaymentController(@Qualifier("taskDispatchQueue") TaskQueue taskQueue
            , @Qualifier("orderDispatchQueue") TaskQueue orderQueue) {
        this.taskQueue = taskQueue;
        this.orderQueue = orderQueue;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        Message mac = new Response().setMessage("PaymentController: " + message);
        ConsolePrintTask consolePrintTask = new ConsolePrintTask();
        consolePrintTask.setMessage(mac);
        taskQueue.add(consolePrintTask);
        return new ResponseEntity(message, HttpStatus.OK);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Response> checkout(@RequestBody Event checkout) {
        Response response = (Response) new Response().setEvent(checkout);
        ConsolePrintTask console = new ConsolePrintTask();
        console.setMessage(response);
        orderQueue.add(console);
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
