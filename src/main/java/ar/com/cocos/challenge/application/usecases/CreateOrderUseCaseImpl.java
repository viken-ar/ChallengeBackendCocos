package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.command.CreateOrderCommand;
import ar.com.cocos.challenge.application.inbound.CreateOrderUseCase;
import ar.com.cocos.challenge.application.outbound.EventPublisherPort;
import ar.com.cocos.challenge.application.outbound.IdempotencyRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.application.strategy.OrderExecutionStrategy;
import ar.com.cocos.challenge.domain.exception.BadDataException;
import ar.com.cocos.challenge.domain.exception.IdempotencyConflictException;
import ar.com.cocos.challenge.domain.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCaseImpl.class);
    private final Map<OrderType, OrderExecutionStrategy> strategyByType;
    private final OrderRepositoryPort orderRepositoryPort;
    private final IdempotencyRepositoryPort idempotencyRepositoryPort;
    private final ObjectMapper objectMapper;
    private final EventPublisherPort eventPublisherPort;

    public CreateOrderUseCaseImpl(
            Map<OrderType, OrderExecutionStrategy> strategyByType,
            OrderRepositoryPort orderRepositoryPort,
            IdempotencyRepositoryPort idempotencyRepositoryPort,
            ObjectMapper objectMapper,
            EventPublisherPort eventPublisherPort
    ) {
        this.strategyByType = strategyByType;
        this.orderRepositoryPort = orderRepositoryPort;
        this.idempotencyRepositoryPort = idempotencyRepositoryPort;
        this.objectMapper = objectMapper;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public Order createOrder(CreateOrderCommand command) {

        if (command.idempotencyKey() == null) {
            throw new BadDataException("Header 'Idempotency-Key' is required and must be a valid UUID");
        }

        String requestHash = computeRequestHash(command);

        Optional<Idempotency> existingRecordOpt = idempotencyRepositoryPort.findByKey(command.idempotencyKey());
        if (existingRecordOpt.isPresent()) {
            Idempotency existing = existingRecordOpt.get();
            return validateAndReturnCachedResponse(command, existing, requestHash);
        }

        String responseJson;
        try {
            responseJson = objectMapper.writeValueAsString(command.getOrder());
        } catch (JsonProcessingException e) {
            log.error("Error serializing idempotent response", e);
            responseJson = StringUtils.EMPTY;
        }
        try {
            idempotencyRepositoryPort.save(new Idempotency(
                    command.idempotencyKey(),
                    command.userId(),
                    requestHash,
                    responseJson,
                    IdempotencyStatus.PROCESSING,
                    Instant.now()));
        } catch (DataIntegrityViolationException e) {
            log.error("Error saving idempotency record", e);
            throw new IdempotencyConflictException("Idempotency key " + command.idempotencyKey() + " is already in use");
        }

        Result<Order> result = strategyByType.get(command.type()).execute(command.getOrder());

        if (result.isFailure()) {
            idempotencyRepositoryPort.updateStatus(command.idempotencyKey(), IdempotencyStatus.FAILED, null);
            throw new BadDataException("Error creating order ->" + result.getError());
        }

        Order savedOrder = orderRepositoryPort.save(result.getData());

        if ((savedOrder.getSide() == OrderSide.BUY || savedOrder.getSide() == OrderSide.SELL)
                && savedOrder.getStatus() == OrderStatus.FILLED) {
                eventPublisherPort.publish(savedOrder);
        }

        try {
            responseJson = objectMapper.writeValueAsString(savedOrder);
        } catch (Exception e) {
            log.error("Error serializing idempotent response", e);
            responseJson = StringUtils.EMPTY;
        }
        idempotencyRepositoryPort.updateStatus(command.idempotencyKey(), IdempotencyStatus.COMPLETED, responseJson);

        return savedOrder;
    }

    private Order validateAndReturnCachedResponse(CreateOrderCommand command, Idempotency idempotency, String requestHash) {
        if (requestHash.equals(idempotency.requestHash())) {
            try {
                return objectMapper.readValue(idempotency.responseBody(), Order.class);
            } catch (Exception e) {
                throw new IllegalStateException("Error reading stored idempotent response", e);
            }
        } else {
            throw new IdempotencyConflictException("Idempotency key " + command.idempotencyKey() + " already exists with a different request");
        }
    }

    private String computeRequestHash(CreateOrderCommand command) {
        try {
            String json = objectMapper.writeValueAsString(command);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Error computing request hash", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
