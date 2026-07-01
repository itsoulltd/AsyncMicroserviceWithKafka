package com.infoworks.saga;

import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.utils.eventq.EventQueue;
import com.infoworks.tasks.*;
import com.infoworks.tasks.models.OptStatus;
import com.infoworks.tasks.models.OrderResponse;
import com.infoworks.tasks.models.PaymentResponse;
import com.infoworks.tasks.models.ShipmentResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Saga Pattern is a design pattern used in distributed systems and microservices
 * architecture to manage long-running transactions and ensure data consistency across
 * services without using distributed transactions (2PC).
 */
public class ChoreographedSagaTest {

    /**
     * A saga is a sequence of local transactions. Each transaction updates one service and
     * publishes an event or sends a message to trigger the next step.
     * If a step fails, the saga executes compensating transactions (rollbacks) for the previous steps.
     *
     * Choreography (Event-Based)
     * • Services listen to events and react.
     * • No central coordinator.
     * • Each service emits events to trigger the next service.
     * Pros:
     * • Decentralized
     * • Simple to implement with messaging/event buses
     * Cons:
     * • Harder to manage and monitor
     * • Difficult to debug
     * • Event ordering issues
     *
     * If a step fails, the service must emit a compensating event.
     *
     * Compensation:
     * When something fails, each completed step must undo its work. That’s done with
     * compensating transactions — logic to reverse what was done (e.g., refund payment, restock
     * items).
     * This means you must explicitly code for rollback in each service.
     */

    static Logger LOG = LoggerFactory.getLogger("ChoreographedSagaTest");

    @Test
    public void choreographedSagaTest() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(7);

        //Application Service Pipelines:-
        TaskQueue orderService = new EventQueue(Executors.newFixedThreadPool(3), true);
        TaskQueue paymentService = new EventQueue(Executors.newSingleThreadExecutor());
        TaskQueue shippingService = new EventQueue(Executors.newFixedThreadPool(5), true);

        //State Management or Choreograph:-
        //Order Choreograph:
        orderService.onTaskComplete((message, state) -> {
            //Order-Flow:
            if (message instanceof OrderResponse) {
                OrderResponse response = (OrderResponse) message;
                if (response.getOptStatus() == OptStatus.CREATE) {
                    paymentService.add(new PaymentTask(response.getOrderID(), response.getMessage()));
                    counter.incrementAndGet();
                } else {
                    counter.decrementAndGet();
                }
            } else {
                //TODO: When Failed
            }
            //Opt:-
            if (counter.get() == 1) latch.countDown();
        });
        //
        //Payment Choreograph:
        paymentService.onTaskComplete((message, state) -> {
            //Payment-Flow:
            if (message instanceof PaymentResponse) {
                PaymentResponse response = (PaymentResponse) message;
                if (response.getOptStatus() == OptStatus.CREATE) {
                    shippingService.add(new ShipmentTask(response.getOrderID(), response.getPaymentID(), response.getMessage()));
                    counter.incrementAndGet();
                } else if (response.getOptStatus() == OptStatus.CANCEL) {
                    orderService.add(new OrderCancelTask(response.getOrderID(), response.getMessage()));
                    counter.incrementAndGet();
                } else {
                    counter.decrementAndGet();
                }
            } else {
                //TODO: When Failed
            }
            //Opt:-
            if (counter.get() == 1) latch.countDown();
        });
        //
        //Shipping Choreograph:
        shippingService.onTaskComplete((message, state) -> {
            //Shipping-Flow:
            if (message instanceof ShipmentResponse) {
                ShipmentResponse response = (ShipmentResponse) message;
                if (response.getOptStatus() == OptStatus.CREATE) {
                    LOG.info("\uD83D\uDE0E " + "[order-id: " + response.getOrderID() + "] "
                            + "==>|| Shipping Complete For OrderID:" + response.getOrderID() + " (" + response.getMessage() + ") ||<==");
                    counter.decrementAndGet();
                } else if(response.getOptStatus() == OptStatus.CANCEL) {
                    paymentService.add(new PaymentCancelTask(response.getOrderID(), response.getPaymentID(), response.getMessage()));
                    counter.incrementAndGet();
                } else {
                    counter.decrementAndGet();
                }
            } else {
                //TODO: When Failed
            }
            //Opt:-
            if (counter.get() == 1) latch.countDown();
        });
        ///////Choreograph///////
        Random random = new Random();
        int orderId = 0;
        orderService.add(new OrderTask(++orderId + "", "Order For Coffee + Croissant", random.nextBoolean()));
        orderService.add(new OrderTask(++orderId + "", "Order For Biscuit & Cake", random.nextBoolean()));
        orderService.add(new OrderTask(++orderId + "", "Order For Glossary", random.nextBoolean()));
        orderService.add(new OrderTask(++orderId + "", "Order For Fruits", random.nextBoolean()));
        orderService.add(new OrderTask(++orderId + "", "Order For Tea + Coffee", random.nextBoolean()));
        orderService.add(new OrderTask(++orderId + "", "Order For Black Coffee", random.nextBoolean()));
        orderService.add(new OrderTask(++orderId + "", "Order For Breakfast", random.nextBoolean()));
        /////////////////////////
        //
        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {}
    }

}
