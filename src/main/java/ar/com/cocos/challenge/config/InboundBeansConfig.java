package ar.com.cocos.challenge.config;

import ar.com.cocos.challenge.application.inbound.CancelOrderUseCase;
import ar.com.cocos.challenge.application.inbound.CreateOrderUseCase;
import ar.com.cocos.challenge.application.inbound.GetPortfolioUseCase;
import ar.com.cocos.challenge.application.inbound.GetUserOrderByIdUseCase;
import ar.com.cocos.challenge.application.inbound.GetUserOrdersUseCase;
import ar.com.cocos.challenge.application.inbound.SearchInstrumentsUseCase;
import ar.com.cocos.challenge.application.outbound.EventPublisherPort;
import ar.com.cocos.challenge.application.outbound.IdempotencyRepositoryPort;
import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.application.outbound.UserRepositoryPort;
import ar.com.cocos.challenge.application.strategy.CreateLimitOrderExecutionStrategy;
import ar.com.cocos.challenge.application.strategy.ExecuteLimitOrderExecutionStrategy;
import ar.com.cocos.challenge.application.strategy.MarketOrderExecutionStrategy;
import ar.com.cocos.challenge.application.strategy.OrderExecutionStrategy;
import ar.com.cocos.challenge.application.usecases.CancelOrderUseCaseImpl;
import ar.com.cocos.challenge.application.usecases.CreateOrderUseCaseImpl;
import ar.com.cocos.challenge.application.usecases.GetPortfolioUseCaseImpl;
import ar.com.cocos.challenge.application.usecases.GetUserOrderByIdUseCaseImpl;
import ar.com.cocos.challenge.application.usecases.GetUserOrdersUseCaseImpl;
import ar.com.cocos.challenge.application.usecases.SearchInstrumentsUseCaseImpl;
import ar.com.cocos.challenge.domain.model.ActionOrder;
import ar.com.cocos.challenge.domain.model.OrderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class InboundBeansConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public SearchInstrumentsUseCase searchInstrumentsUseCase(
            InstrumentRepositoryPort instrumentRepositoryPort,
            MarketDataRepositoryPort marketDataRepositoryPort
    ) {
        return new SearchInstrumentsUseCaseImpl(instrumentRepositoryPort, marketDataRepositoryPort);
    }

    @Bean
    public GetPortfolioUseCase getPortfolioUseCase(
            UserRepositoryPort userRepositoryPort,
            OrderRepositoryPort orderRepositoryPort,
            InstrumentRepositoryPort instrumentRepositoryPort,
            MarketDataRepositoryPort marketDataRepositoryPort
    ) {
        return new GetPortfolioUseCaseImpl(
                userRepositoryPort,
                orderRepositoryPort,
                instrumentRepositoryPort,
                marketDataRepositoryPort
        );
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(MarketOrderExecutionStrategy marketStrategy,
                                                 CreateLimitOrderExecutionStrategy limitStrategy,
                                                 OrderRepositoryPort orderRepositoryPort,
                                                 IdempotencyRepositoryPort idempotencyRepositoryPort,
                                                 ObjectMapper objectMapper,
                                                 EventPublisherPort eventPublisherPort
    ) {

        Map<OrderType, OrderExecutionStrategy> strategyByType = Map.of(
                OrderType.MARKET, marketStrategy,
                OrderType.LIMIT, limitStrategy
        );

        return new CreateOrderUseCaseImpl(
                strategyByType,
                orderRepositoryPort,
                idempotencyRepositoryPort,
                objectMapper,
                eventPublisherPort
        );
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(
            OrderRepositoryPort orderRepositoryPort
    ) {
        return new CancelOrderUseCaseImpl(orderRepositoryPort);
    }

    @Bean
    public GetUserOrdersUseCase getUserOrdersUseCase(
            OrderRepositoryPort orderRepositoryPort
    ) {
        return new GetUserOrdersUseCaseImpl(orderRepositoryPort);
    }

    @Bean
    public GetUserOrderByIdUseCase getUserOrderByIdUseCase(
            OrderRepositoryPort orderRepositoryPort
    ) {
        return new GetUserOrderByIdUseCaseImpl(orderRepositoryPort);
    }


    @Bean
    public MarketOrderExecutionStrategy marketOrderExecutionStrategy(
            OrderRepositoryPort orderRepository,
            MarketDataRepositoryPort marketDataRepositoryPort
    ) {
        return new MarketOrderExecutionStrategy(orderRepository, marketDataRepositoryPort);
    }

    @Bean
    public CreateLimitOrderExecutionStrategy limitOrderExecutionStrategy(
            OrderRepositoryPort orderRepository,
            MarketDataRepositoryPort marketDataRepositoryPort
    ) {
        return new CreateLimitOrderExecutionStrategy(orderRepository, marketDataRepositoryPort);
    }

    @Bean
    public ExecuteLimitOrderExecutionStrategy executeLimitOrderExecutionStrategy(
            OrderRepositoryPort orderRepository,
            MarketDataRepositoryPort marketDataRepositoryPort
    ) {
        return new ExecuteLimitOrderExecutionStrategy(orderRepository, marketDataRepositoryPort);
    }


}
