package com.infoworks.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.utils.jmsq.JmsMessage;

public class JmsMessageUtil {

    public static JmsMessage convert(Task task, ObjectMapper mapper){
        return JmsMessage.convert(task, mapper);
    }

    public static JmsMessage convert(Task task, Message error, ObjectMapper mapper){
        return JmsMessage.convert(task, error, mapper);
    }

    public static Task revert(String text) throws RuntimeException {
        return JmsMessage.revert(text);
    }

}
