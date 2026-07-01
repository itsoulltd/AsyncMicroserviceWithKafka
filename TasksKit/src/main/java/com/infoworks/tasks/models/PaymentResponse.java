package com.infoworks.tasks.models;

import com.infoworks.objects.Response;

/**
 *
 */
public class PaymentResponse extends Response {
    private String orderID;
    private String paymentID;
    private OptStatus optStatus = OptStatus.NONE;

    public String getOrderID() {
        return orderID;
    }

    public PaymentResponse setOrderID(String orderID) {
        this.orderID = orderID;
        return this;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public PaymentResponse setPaymentID(String paymentID) {
        this.paymentID = paymentID;
        return this;
    }

    public OptStatus getOptStatus() {
        return optStatus;
    }

    public PaymentResponse setOptStatus(OptStatus optStatus) {
        this.optStatus = optStatus;
        return this;
    }
}
