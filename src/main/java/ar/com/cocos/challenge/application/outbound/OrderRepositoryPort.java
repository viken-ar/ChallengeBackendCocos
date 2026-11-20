package ar.com.cocos.challenge.application.outbound;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.OrderType;
import ar.com.cocos.challenge.domain.model.Page;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    Optional<Order> findById(Integer id);
    List<Order> findByUserId(Integer userId);
    Page<Order> findByUserIdPaginated(Integer userId, int page, int limit);
    List<Order> findByUserIdAndStatus(Integer userId, OrderStatus status);
    List<Order> findByUserIdAndInstrumentIdAndStatus(Integer userId, Integer instrumentId, OrderStatus status);
    List<Order> findByTypeAndStatus(OrderType type, OrderStatus status);
    Order save(Order order);
}
