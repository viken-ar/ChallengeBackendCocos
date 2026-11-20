package ar.com.cocos.challenge.application.outbound;

import ar.com.cocos.challenge.domain.model.Order;

/**
 * Outbound port for publishing domain events.
 * Allows the application layer to publish events without depending on a specific framework.
 */
public interface EventPublisherPort {

    /**
     * Publishes a domain event to interested listeners.
     * @param event the domain event to publish
     */
    void publish(Order event);
}
