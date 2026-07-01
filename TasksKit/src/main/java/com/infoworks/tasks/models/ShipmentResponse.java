package com.infoworks.tasks.models;

import com.infoworks.objects.Response;

/**
 *
 */
public class ShipmentResponse extends Response {
    private String orderID;
    private String paymentID;
    private String shippingID;
    private OptStatus optStatus = OptStatus.NONE;

    public String getOrderID() {
        return orderID;
    }

    public ShipmentResponse setOrderID(String orderID) {
        this.orderID = orderID;
        return this;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public ShipmentResponse setPaymentID(String paymentID) {
        this.paymentID = paymentID;
        return this;
    }

    public String getShippingID() {
        return shippingID;
    }

    public ShipmentResponse setShippingID(String shippingID) {
        this.shippingID = shippingID;
        return this;
    }

    public OptStatus getOptStatus() {
        return optStatus;
    }

    public ShipmentResponse setOptStatus(OptStatus optStatus) {
        this.optStatus = optStatus;
        return this;
    }
}
