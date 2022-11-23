package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskCompletionListener;
import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.tasks.ConsolePrintTask;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class DeliveryController implements TaskCompletionListener {

    private static Logger LOG = LoggerFactory.getLogger(DeliveryController.class.getSimpleName());
    private TaskQueue taskQueue;

    public DeliveryController(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    @GetMapping("/print/{message}")
    public ResponseEntity<String> print(@PathVariable("message") final String message){
        Message mac = new Response().setMessage("DeliveryController: " + message);
        ConsolePrintTask consolePrintTask = new ConsolePrintTask();
        consolePrintTask.setMessage(mac);
        taskQueue.add(consolePrintTask);
        return new ResponseEntity(message, HttpStatus.OK);
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
