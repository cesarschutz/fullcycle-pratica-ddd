package com.fullcycle.ticketsales.domain.order.events;

import com.fullcycle.ticketsales.domain.common.DomainEvent;
import com.fullcycle.ticketsales.domain.order.OrderStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class OrderCancelled implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final String orderId;
    private final OrderStatus status;

    public OrderCancelled(String orderId, OrderStatus status) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.orderId = orderId;
        this.status = status;
    }

    @Override
    public String getEventName() {
        return "OrderCancelled";
    }
}
