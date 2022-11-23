package com.infoworks.lab.domain.tasks;

import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.events.Event;

public class DeliveryDispatchTask extends BaseDeliveryTask<Message, Response> {

    @Override
    public Response execute(Message message) throws RuntimeException {
        Event confirm = new Event();
        Response response = (Response) new Response().setEvent(confirm);
        ConsolePrintTask console = new ConsolePrintTask();
        console.setMessage(response);
        getDeliveryQueue().add(console);
        return response;
    }
}
