package com.infoworks.lab.domain.queue;

import com.infoworks.lab.beans.queue.AbstractTaskQueue;
import com.infoworks.lab.beans.queue.JmsMessage;
import com.infoworks.lab.beans.tasks.definition.Task;
import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.rest.models.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("deliveryDispatchQueue")
public class DeliveryDispatchQueue extends AbstractTaskQueue {

    private KafkaTemplate<String, String> kafkaTemplate;

    public DeliveryDispatchQueue(@Qualifier("kafkaTextTemplate") KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${topic.delivery.execute}")
    private String exeQueue;

    @Value("${topic.delivery.abort}")
    private String abortQueue;

    @Override
    public TaskQueue add(Task task) {
        //Defined:JmsMessage Protocol
        JmsMessage jmsMessage = convert(task);
        kafkaTemplate.send(exeQueue, jmsMessage.toString());
        return this;
    }

    @Override
    public void abort(Task task, Message error) {
        //Defined:JmsMessage Protocol
        JmsMessage jmsMessage = convert(task, error);
        kafkaTemplate.send(abortQueue, jmsMessage.toString());
    }

    @Override
    public TaskQueue cancel(Task task) {
        //TODO:
        return this;
    }

}
