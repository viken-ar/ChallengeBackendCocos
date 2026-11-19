package ar.com.cocos.challenge.application.inbound;

import ar.com.cocos.challenge.application.command.CreateOrderCommand;
import ar.com.cocos.challenge.domain.model.Order;

public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}
