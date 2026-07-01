package com.infoworks.tasks.models;

import com.infoworks.objects.Response;

/**
 *
 */
public class OrderResponse extends Response {
    private String orderID;
    private OptStatus optStatus = OptStatus.NONE;

    public String getOrderID() {
        return orderID;
    }

    public OrderResponse setOrderID(String orderID) {
        this.orderID = orderID;
        return this;
    }

    public OptStatus getOptStatus() {
        return optStatus;
    }

    public OrderResponse setOptStatus(OptStatus optStatus) {
        this.optStatus = optStatus;
        return this;
    }
}
