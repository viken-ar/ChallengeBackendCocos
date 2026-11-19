package ar.com.cocos.challenge.application.inbound;

import ar.com.cocos.challenge.domain.model.Order;

public interface GetUserOrderByIdUseCase {
    Order getUserOrderById(Integer userId, Integer orderId);
}
