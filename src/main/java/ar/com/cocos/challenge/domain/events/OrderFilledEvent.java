package ar.com.cocos.challenge.domain.events;

import ar.com.cocos.challenge.domain.model.Order;

/**
 * Domain event published when an order (BUY/SELL) is filled.
 * Triggers the creation of a corresponding CASH_IN or CASH_OUT order.
 * This is a plain domain event with no framework dependencies.
 */
public class OrderFilledEvent {

    private final Order order;

    public OrderFilledEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
