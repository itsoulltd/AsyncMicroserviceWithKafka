package com.infoworks.tasks;

import com.infoworks.objects.Message;
import com.infoworks.orm.Property;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OrderCancelTask extends ExecutableTask<Message, OrderResponse> {

    protected static Logger LOG = LoggerFactory.getLogger("OrderCancelTask");

    public OrderCancelTask() {}

    public OrderCancelTask(String orderId, String message) {
        super(new Property("message", message)
                , new Property("orderId", orderId));
    }

    @Override
    public OrderResponse execute(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = "[order-id: " + orderId + "] " + strMsg;
        //True will be Success, failed other-wise:
        LOG.info("⛔ " + msg + "  ==>  " + "Commit: Order Cancel In DB [" + Thread.currentThread().getName() + "]");
        return (OrderResponse) new OrderResponse().setOptStatus(OptStatus.CANCEL).setOrderID(orderId).setStatus(200).setMessage(strMsg);
    }
}
