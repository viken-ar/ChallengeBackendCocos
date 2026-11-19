package ar.com.cocos.challenge.application.inbound;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.Page;

public interface GetUserOrdersUseCase {
    Page<Order> getUserOrders(Integer userId, int page, int limit);
}
