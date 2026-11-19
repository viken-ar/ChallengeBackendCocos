package ar.com.cocos.challenge.infrastructure.database.adapter;

import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.Page;
import ar.com.cocos.challenge.infrastructure.database.entities.InstrumentEntity;
import ar.com.cocos.challenge.infrastructure.database.entities.OrderEntity;
import ar.com.cocos.challenge.infrastructure.database.mappers.OrderInfrastructureMapper;
import ar.com.cocos.challenge.infrastructure.database.repositories.InstrumentJpaRepository;
import ar.com.cocos.challenge.infrastructure.database.repositories.OrderJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final InstrumentJpaRepository instrumentJpaRepository;
    private final OrderInfrastructureMapper orderInfrastructureMapper;

    public OrderRepositoryAdapter(
            OrderJpaRepository orderJpaRepository,
            InstrumentJpaRepository instrumentJpaRepository,
            OrderInfrastructureMapper orderInfrastructureMapper
    ) {
        this.orderJpaRepository = orderJpaRepository;
        this.instrumentJpaRepository = instrumentJpaRepository;
        this.orderInfrastructureMapper = orderInfrastructureMapper;
    }

    @Override
    public Optional<Order> findById(Integer id) {
        return orderJpaRepository.findById(id).map(orderInfrastructureMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(Integer userId) {
        return orderInfrastructureMapper.toDomainList(orderJpaRepository.findByUserId(userId));
    }

    @Override
    public Page<Order> findByUserIdPaginated(Integer userId, int page, int limit) {
        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, limit);
        var springPage = orderJpaRepository.findByUserId(userId, pageable);
        List<Order> content = orderInfrastructureMapper.toDomainList(springPage.getContent());
        return new Page<>(
                content,
                springPage.isFirst(),
                springPage.isLast(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }

    @Override
    public List<Order> findByUserIdAndStatus(Integer userId, OrderStatus status) {
        return orderInfrastructureMapper.toDomainList(orderJpaRepository.findByUserIdAndStatus(userId, status));
    }

    @Override
    public List<Order> findByUserIdAndInstrumentIdAndStatus(Integer userId, Integer instrumentId, OrderStatus status) {
        return orderInfrastructureMapper.toDomainList(
                orderJpaRepository.findByUserIdAndInstrument_IdAndStatus(userId, instrumentId, status)
        );
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderInfrastructureMapper.toEntity(order);
        InstrumentEntity instrumentEntity = instrumentJpaRepository.findById(order.getInstrumentId())
                .orElse(null);
        entity.setInstrument(instrumentEntity);
        OrderEntity saved = orderJpaRepository.save(entity);
        return orderInfrastructureMapper.toDomain(saved);
    }
}
