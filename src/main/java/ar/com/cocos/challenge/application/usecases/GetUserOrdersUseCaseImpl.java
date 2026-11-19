package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.inbound.GetUserOrdersUseCase;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.Page;

public class GetUserOrdersUseCaseImpl implements GetUserOrdersUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public GetUserOrdersUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public Page<Order> getUserOrders(Integer userId, int page, int limit) {
        return orderRepositoryPort.findByUserIdPaginated(userId, page, limit);
    }
}
