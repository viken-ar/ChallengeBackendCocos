package ar.com.cocos.challenge.infrastructure.database.repositories;

import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.OrderType;
import ar.com.cocos.challenge.infrastructure.database.entities.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Integer>, JpaSpecificationExecutor<OrderEntity> {
    List<OrderEntity> findByUserId(Integer userId);
    Page<OrderEntity> findByUserId(Integer userId, Pageable pageable);
    List<OrderEntity> findByUserIdAndStatus(Integer userId, OrderStatus status);
    List<OrderEntity> findByUserIdAndInstrumentIdAndStatus(Integer userId, Integer instrumentId, OrderStatus status);
    List<OrderEntity> findByUserIdAndInstrument_IdAndStatus(Integer userId, Integer instrumentId, OrderStatus status);
    List<OrderEntity> findByTypeAndStatus(OrderType type, OrderStatus status);
}
