package com.infoworks.tasks;

import com.infoworks.objects.Message;
import com.infoworks.orm.Property;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

/**
 *
 */
public class PaymentTask extends BaseTask<Message, PaymentResponse> {

    protected static Logger LOG = LoggerFactory.getLogger("PaymentTask");

    public PaymentTask() {}

    public PaymentTask(String orderId, String message, boolean nextRandom) {
        super(new Property("message", message)
                , new Property("orderId", orderId)
                , new Property("nextRandom", nextRandom));
    }

    public PaymentTask(String orderId, String message) {
        this(orderId, message, new Random().nextBoolean());
    }

    @Override
    public PaymentResponse execute(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = "[order-id: " + orderId + "] " + strMsg;
        boolean nextRandom = (getPropertyValue("nextRandom") != null)
                ? Boolean.parseBoolean(getPropertyValue("nextRandom").toString())
                : true;
        //True will be Success, failed other-wise:
        if (nextRandom) {
            String paymentID = UUID.randomUUID().toString(); //GENERATED FROM DATABASE
            /**
             * All your payment tasks:
             */
            LOG.info("✅ " + msg + "  ==>  " + "Commit: Payment Create In DB [" + Thread.currentThread().getName() + "]");
            closeDbConnections();
            return (PaymentResponse) new PaymentResponse().setOptStatus(OptStatus.CREATE).setPaymentID(paymentID).setOrderID(orderId).setStatus(200).setMessage(strMsg);
        } else {
            closeDbConnections();
            throw new RuntimeException(msg);
        }
    }

    @Override
    public PaymentResponse abort(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = "[order-id: " + orderId + "] " + strMsg;
        LOG.info("❌ " + msg + "  ==>  " + "Commit: Payment Create Failed In DB [" + Thread.currentThread().getName() + "]");
        closeDbConnections();
        return (PaymentResponse) new PaymentResponse().setOptStatus(OptStatus.CANCEL).setOrderID(orderId).setStatus(500).setMessage(strMsg);
    }
}
