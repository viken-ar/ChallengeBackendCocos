package ar.com.cocos.challenge.application.inbound;

import ar.com.cocos.challenge.domain.model.Order;

public interface CancelOrderUseCase {
    Order cancelOrder(Integer orderId);
}
