package com.infoworks.lab.domain.queue;

import com.infoworks.tasks.BaseTask;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.QueuedTaskStateListener;
import com.infoworks.utils.jmsq.AbstractJmsQueueManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

@Component
public class TaskQueueManager extends AbstractJmsQueueManager {

    private static final Logger logger = Logger.getLogger("TaskQueueManager");

    public TaskQueueManager(@Qualifier("taskDispatchQueue") QueuedTaskStateListener listener) {
        super(listener);
    }

    @Override
    protected Task createTask(String text) throws ClassNotFoundException, IOException
            , IllegalAccessException, InstantiationException
            , NoSuchMethodException, InvocationTargetException {
        Task task = super.createTask(text);
        //Inject dependency into Task during MOM's task execution.
        if (task instanceof BaseTask) {
            //
        }
        return task;
    }

    @KafkaListener(topics = {"${topic.delivery.execute}"}, concurrency = "5")
    public void startListener(@Payload String message, Acknowledgment ack) {
        // retrieve the message content
        String text = message;
        //logger.log(Level.INFO, "EXE-QUEUE: Message received {0} ", text);
        if (handleTextOnStart(text)){
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = {"${topic.delivery.abort}"}, concurrency = "3")
    public void abortListener(@Payload String message, Acknowledgment ack) {
        // retrieve the message content
        String text = message;
        //logger.log(Level.INFO, "ABORT-QUEUE: Message received {0} ", text);
        if (handleTextOnStop(text)){
            ack.acknowledge();
        }
    }

}
