package ar.com.cocos.challenge.config;

import ar.com.cocos.challenge.application.outbound.EventPublisherPort;
import ar.com.cocos.challenge.application.outbound.IdempotencyRepositoryPort;
import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.application.outbound.UserRepositoryPort;
import ar.com.cocos.challenge.infrastructure.database.adapter.IdempotencyRepositoryAdapter;
import ar.com.cocos.challenge.infrastructure.database.adapter.InstrumentRepositoryAdapter;
import ar.com.cocos.challenge.infrastructure.database.adapter.MarketDataRepositoryAdapter;
import ar.com.cocos.challenge.infrastructure.database.adapter.OrderRepositoryAdapter;
import ar.com.cocos.challenge.infrastructure.database.adapter.UserRepositoryAdapter;
import ar.com.cocos.challenge.infrastructure.event.adapter.SpringEventPublisherAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OutboundBeansConfig {

    @Bean
    @Primary
    public UserRepositoryPort userRepositoryPort(UserRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    @Primary
    public InstrumentRepositoryPort instrumentRepositoryPort(InstrumentRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    @Primary
    public MarketDataRepositoryPort marketDataRepositoryPort(MarketDataRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    @Primary
    public OrderRepositoryPort orderRepositoryPort(OrderRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    @Primary
    public IdempotencyRepositoryPort idempotencyRepositoryPort(IdempotencyRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    @Primary
    public EventPublisherPort eventPublisherPort(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringEventPublisherAdapter(applicationEventPublisher);
    }
}
