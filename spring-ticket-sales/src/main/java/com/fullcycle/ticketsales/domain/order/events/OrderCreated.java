package com.fullcycle.ticketsales.domain.order.events;

import com.fullcycle.ticketsales.domain.common.DomainEvent;
import com.fullcycle.ticketsales.domain.order.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class OrderCreated implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final String orderId;
    private final String customerId;
    private final String eventSpotId;
    private final BigDecimal amount;
    private final OrderStatus status;

    public OrderCreated(String orderId, String customerId, String eventSpotId,
                        BigDecimal amount, OrderStatus status) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.orderId = orderId;
        this.customerId = customerId;
        this.eventSpotId = eventSpotId;
        this.amount = amount;
        this.status = status;
    }

    @Override
    public String getEventName() {
        return "OrderCreated";
    }
}
