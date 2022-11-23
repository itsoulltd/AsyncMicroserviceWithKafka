package com.infoworks.lab.domain.tasks;

import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.beans.tasks.nuts.ExecutableTask;
import com.infoworks.lab.rest.models.Message;

public abstract class BaseDeliveryTask<In extends Message, Out extends Message> extends ExecutableTask<In, Out> {

    private TaskQueue deliveryQueue;

    public void setDeliveryQueue(TaskQueue deliveryQueue) {
        this.deliveryQueue = deliveryQueue;
    }

    public TaskQueue getDeliveryQueue() {
        return deliveryQueue;
    }
}
