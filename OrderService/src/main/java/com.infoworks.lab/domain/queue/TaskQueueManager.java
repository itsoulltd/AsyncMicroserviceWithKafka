package com.infoworks.lab.domain.queue;

import com.infoworks.lab.beans.queue.AbstractTaskQueueManager;
import com.infoworks.lab.beans.tasks.definition.QueuedTaskLifecycleListener;
import com.infoworks.lab.beans.tasks.definition.Task;
import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.tasks.BaseDeliveryTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TaskQueueManager extends AbstractTaskQueueManager {

    private static final Logger logger = Logger.getLogger("TaskQueueManager");
    private TaskQueue deliveryQ;

    public TaskQueueManager(@Autowired QueuedTaskLifecycleListener listener
            , @Qualifier("deliveryDispatchQueue") TaskQueue deliveryQ) {
        super(listener);
        this.deliveryQ = deliveryQ;
    }

    @Override
    protected Task createTask(String text) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Task task = super.createTask(text);
        //Inject dependency into Task during MOM's task execution.
        if (task instanceof BaseDeliveryTask)
            ((BaseDeliveryTask) task).setDeliveryQueue(deliveryQ);
        return task;
    }

    @KafkaListener(topics = {"${topic.execute}"}, concurrency = "5")
    public void startListener(@Payload String message, Acknowledgment ack) {
        // retrieve the message content
        String text = message;
        logger.log(Level.INFO, "EXE-QUEUE: Message received {0} ", text);
        if (handleTextOnStart(text)){
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = {"${topic.abort}"}, concurrency = "3")
    public void abortListener(@Payload String message, Acknowledgment ack) {
        // retrieve the message content
        String text = message;
        logger.log(Level.INFO, "ABORT-QUEUE: Message received {0} ", text);
        if (handleTextOnStop(text)){
            ack.acknowledge();
        }
    }

}
