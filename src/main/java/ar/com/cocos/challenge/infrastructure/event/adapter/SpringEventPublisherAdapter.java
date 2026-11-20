package ar.com.cocos.challenge.infrastructure.event.adapter;

import ar.com.cocos.challenge.application.outbound.EventPublisherPort;
import ar.com.cocos.challenge.domain.events.OrderFilledEvent;
import ar.com.cocos.challenge.domain.model.Order;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Infrastructure adapter that implements {@link EventPublisherPort} using Spring's
 * {@link ApplicationEventPublisher}. This bridges the hexagonal port to the Spring framework.
 */
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Order order) {
        applicationEventPublisher.publishEvent(new OrderFilledEvent(order));
    }
}
