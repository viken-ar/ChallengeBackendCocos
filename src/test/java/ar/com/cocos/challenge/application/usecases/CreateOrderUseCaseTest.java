package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.command.CreateOrderCommand;
import ar.com.cocos.challenge.application.outbound.EventPublisherPort;
import ar.com.cocos.challenge.application.outbound.IdempotencyRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.application.strategy.OrderExecutionStrategy;
import ar.com.cocos.challenge.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderExecutionStrategy marketStrategy;

    @Mock
    private OrderExecutionStrategy limitStrategy;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private IdempotencyRepositoryPort idempotencyRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private ObjectMapper objectMapper;
    private Map<OrderType, OrderExecutionStrategy> strategyByType;
    private CreateOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        strategyByType = new HashMap<>();
        strategyByType.put(OrderType.MARKET, marketStrategy);
        strategyByType.put(OrderType.LIMIT, limitStrategy);
        useCase = new CreateOrderUseCaseImpl(
                strategyByType,
                orderRepositoryPort,
                idempotencyRepositoryPort,
                objectMapper,
                eventPublisherPort
        );
    }

    @Test
    void createOrder_whenIdempotencyKeyAlreadyExists_shouldReturnCachedOrder() {
        UUID key = UUID.randomUUID();
        CreateOrderCommand command = new CreateOrderCommand(
                key, 1, 10, OrderSide.BUY, OrderType.MARKET, 10, null, null
        );

        Order cachedOrder = Order.builder()
                .id(999)
                .userId(1)
                .instrumentId(10)
                .size(10)
                .price(new BigDecimal("100.00"))
                .type(OrderType.MARKET)
                .side(OrderSide.BUY)
                .status(OrderStatus.FILLED)
                .datetime(Instant.now())
                .build();

        try {
            String json = objectMapper.writeValueAsString(cachedOrder);
            String requestJson = objectMapper.writeValueAsString(command);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(requestJson.getBytes(StandardCharsets.UTF_8));
            String requestHash = bytesToHex(hashBytes);
            Idempotency record = new Idempotency(key, 1, requestHash, json, IdempotencyStatus.COMPLETED, Instant.now());
            when(idempotencyRepositoryPort.findByKey(key)).thenReturn(Optional.of(record));

            Order result = useCase.createOrder(command);

            assertThat(result.getId()).isEqualTo(999);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.FILLED);
            verify(orderRepositoryPort, never()).save(any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createOrder_marketBuy_withSufficientFunds_shouldExecuteAsFilled() {
        UUID key = UUID.randomUUID();
        CreateOrderCommand command = new CreateOrderCommand(
                key, 1, 10, OrderSide.BUY, OrderType.MARKET, 5, null, null
        );

        Order orderFromCommand = command.getOrder();

        Order executedOrder = Order.builder()
                .userId(1)
                .instrumentId(10)
                .instrumentName("GGAL")
                .size(5)
                .price(new BigDecimal("200.00"))
                .type(OrderType.MARKET)
                .side(OrderSide.BUY)
                .status(OrderStatus.FILLED)
                .datetime(Instant.now())
                .build();

        when(idempotencyRepositoryPort.findByKey(key)).thenReturn(Optional.empty());
        when(marketStrategy.execute(any(Order.class))).thenReturn(Result.success(executedOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.createOrder(command);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(result.getPrice()).isEqualByComparingTo("200.00");
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getInstrumentName()).isEqualTo("GGAL");
        verify(idempotencyRepositoryPort).save(any());
    }

    @Test
    void createOrder_marketBuy_withInsufficientFunds_shouldBeRejected() {
        UUID key = UUID.randomUUID();
        CreateOrderCommand command = new CreateOrderCommand(
                key, 1, 10, OrderSide.BUY, OrderType.MARKET, 100, null, null
        );

        Order executedOrder = Order.builder()
                .userId(1)
                .instrumentId(10)
                .size(100)
                .price(new BigDecimal("200.00"))
                .type(OrderType.MARKET)
                .side(OrderSide.BUY)
                .status(OrderStatus.REJECTED)
                .rejectReason("Insufficient funds")
                .datetime(Instant.now())
                .build();

        when(idempotencyRepositoryPort.findByKey(key)).thenReturn(Optional.empty());
        when(marketStrategy.execute(any(Order.class))).thenReturn(Result.success(executedOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.createOrder(command);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.REJECTED);
        verify(orderRepositoryPort).save(any());
    }

    @Test
    void createOrder_limitBuy_shouldBeNew() {
        UUID key = UUID.randomUUID();
        CreateOrderCommand command = new CreateOrderCommand(
                key, 1, 10, OrderSide.BUY, OrderType.LIMIT, 10, null, new BigDecimal("150.00")
        );

        Order executedOrder = Order.builder()
                .userId(1)
                .instrumentId(10)
                .size(10)
                .price(new BigDecimal("150.00"))
                .type(OrderType.LIMIT)
                .side(OrderSide.BUY)
                .status(OrderStatus.NEW)
                .datetime(Instant.now())
                .build();

        when(idempotencyRepositoryPort.findByKey(key)).thenReturn(Optional.empty());
        when(limitStrategy.execute(any(Order.class))).thenReturn(Result.success(executedOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.createOrder(command);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(result.getPrice()).isEqualByComparingTo("150.00");
    }

    @Test
    void createOrder_byAmount_calculatesFloorSize() {
        UUID key = UUID.randomUUID();
        // Amount $1050 with close price $200 -> size = floor(1050 / 200) = 5
        CreateOrderCommand command = new CreateOrderCommand(
                key, 1, 10, OrderSide.BUY, OrderType.MARKET, null, new BigDecimal("1050.00"), null
        );

        Order executedOrder = Order.builder()
                .userId(1)
                .instrumentId(10)
                .size(5)
                .price(new BigDecimal("200.00"))
                .type(OrderType.MARKET)
                .side(OrderSide.BUY)
                .status(OrderStatus.FILLED)
                .amount(new BigDecimal("1050.00"))
                .datetime(Instant.now())
                .build();

        when(idempotencyRepositoryPort.findByKey(key)).thenReturn(Optional.empty());
        when(marketStrategy.execute(any(Order.class))).thenReturn(Result.success(executedOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.createOrder(command);

        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.FILLED);
    }

    @Test
    void createOrder_withoutIdempotencyKey_shouldThrowBadDataException() {
        CreateOrderCommand command = new CreateOrderCommand(
                null, 1, 10, OrderSide.BUY, OrderType.MARKET, 5, null, null
        );

        assertThatThrownBy(() -> useCase.createOrder(command))
                .isInstanceOf(ar.com.cocos.challenge.domain.exception.BadDataException.class)
                .hasMessageContaining("Idempotency-Key");
    }

    @Test
    void createOrder_whenStrategyReturnsFailure_shouldThrowBadDataException() {
        UUID key = UUID.randomUUID();
        CreateOrderCommand command = new CreateOrderCommand(
                key, 1, 10, OrderSide.BUY, OrderType.MARKET, 5, null, null
        );

        when(idempotencyRepositoryPort.findByKey(key)).thenReturn(Optional.empty());
        when(marketStrategy.execute(any(Order.class))).thenReturn(Result.failure("Invalid order"));

        assertThatThrownBy(() -> useCase.createOrder(command))
                .isInstanceOf(ar.com.cocos.challenge.domain.exception.BadDataException.class)
                .hasMessageContaining("Invalid order");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
