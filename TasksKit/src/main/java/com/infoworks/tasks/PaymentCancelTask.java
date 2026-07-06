package com.infoworks.tasks;

import com.infoworks.objects.Message;
import com.infoworks.orm.Property;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PaymentCancelTask extends BaseTask<Message, PaymentResponse> {

    protected static Logger LOG = LoggerFactory.getLogger("PaymentCancelTask");

    public PaymentCancelTask() {}

    public PaymentCancelTask(String orderId, String paymentId, String message) {
        super(new Property("message", message)
                , new Property("orderId", orderId)
                , new Property("paymentId", paymentId));
    }

    @Override
    public PaymentResponse execute(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String paymentId = getPropertyValue("paymentId").toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = "[order-id: " + orderId + "] " + strMsg;
        //True will be Success, failed other-wise:
        LOG.info("⛔ " + msg + "  ==>  " + "Commit: Payment Cancel In DB [" + Thread.currentThread().getName() + "]");
        return (PaymentResponse) new PaymentResponse().setOptStatus(OptStatus.CANCEL).setPaymentID(paymentId).setOrderID(orderId).setStatus(200).setMessage(strMsg);
    }
}
