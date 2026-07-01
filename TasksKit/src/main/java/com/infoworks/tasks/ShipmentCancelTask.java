package com.infoworks.tasks;

import com.infoworks.objects.Message;
import com.infoworks.orm.Property;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.ShipmentResponse;

/**
 *
 */
public class ShipmentCancelTask extends ExecutableTask<Message, ShipmentResponse> {

    public ShipmentCancelTask() {}

    public ShipmentCancelTask(String orderId, String paymentId, String shipmentId, String message) {
        super(new Property("message", message)
                , new Property("orderId", orderId)
                , new Property("paymentId", paymentId)
                , new Property("shipmentId", shipmentId));
    }

    @Override
    public ShipmentResponse execute(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String paymentId = getPropertyValue("paymentId").toString();
        String shipmentId = getPropertyValue("shipmentId").toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = "[order-id: " + orderId + "] " + strMsg;
        //True will be Success, failed other-wise:
        System.out.println("⛔ " + msg + "  ==>  " + "Commit: Shipment Cancel In DB [" + Thread.currentThread().getName() + "]");
        return (ShipmentResponse) new ShipmentResponse().setOptStatus(OptStatus.CANCEL).setShippingID(shipmentId).setPaymentID(paymentId).setOrderID(orderId).setStatus(200).setMessage(strMsg);
    }
}
