package com.infoworks.tasks;

import com.infoworks.objects.Message;
import com.infoworks.orm.Property;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.PaymentResponse;
import com.infoworks.tasks.models.ShipmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

/**
 *
 */
public class ShipmentTask extends ExecutableTask<Message, ShipmentResponse> {

    protected static Logger LOG = LoggerFactory.getLogger("ShipmentTask");

    public ShipmentTask() {}

    public ShipmentTask(String orderId, String paymentId, String message, boolean nextRandom) {
        super(new Property("message", message)
                , new Property("orderId", orderId)
                , new Property("paymentId", paymentId)
                , new Property("nextRandom", nextRandom));
    }

    public ShipmentTask(String orderId, String paymentId, String message) {
        this(orderId, paymentId, message, new Random().nextBoolean());
    }

    @Override
    public ShipmentResponse execute(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String paymentId = getPropertyValue("paymentId", message).toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = "[order-id: " + orderId + "] " + strMsg;
        boolean nextRandom = (getPropertyValue("nextRandom") != null)
                ? Boolean.parseBoolean(getPropertyValue("nextRandom").toString())
                : true;
        //True will be Success, failed other-wise:
        if (nextRandom) {
            String shipmentID = UUID.randomUUID().toString(); //GENERATED FROM DATABASE
            /**
             * All your shipping tasks:
             */
            LOG.info("✅ " + msg + "  ==>  " + "Commit: Shipment Create In DB [" + Thread.currentThread().getName() + "]");
            return (ShipmentResponse) new ShipmentResponse().setOptStatus(OptStatus.CREATE).setShippingID(shipmentID).setPaymentID(paymentId).setOrderID(orderId).setStatus(200).setMessage(strMsg);
        } else {
            throw new RuntimeException(msg);
        }
    }

    @Override
    public ShipmentResponse abort(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String paymentId = getPropertyValue("paymentId").toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = "[order-id: " + orderId + "] " + strMsg;
        LOG.info("❌ " + msg + "  ==>  " + "Commit: Shipment Create Failed In DB [" + Thread.currentThread().getName() + "]");
        return (ShipmentResponse) new ShipmentResponse().setOptStatus(OptStatus.CANCEL).setPaymentID(paymentId).setOrderID(orderId).setStatus(500).setMessage(strMsg);
    }

    protected Object getPropertyValue(String key, Message message) throws RuntimeException {
        if (key.equalsIgnoreCase("paymentId")) {
            String paymentId = getPropertyValue("paymentId").toString();
            if (paymentId == null || paymentId.isEmpty()) {
                //Let's try to get it from message:
                if (message != null && message instanceof PaymentResponse) {
                    paymentId = ((PaymentResponse) message).getPaymentID();
                    updateProperties(new Property("paymentId", paymentId));
                }
            }
            return paymentId;
        } else {
            return getPropertyValue(key);
        }
    }
}
