package com.fullcycle.ticketsales.domain.order;

import com.fullcycle.ticketsales.domain.common.AggregateRoot;
import com.fullcycle.ticketsales.domain.order.events.OrderCancelled;
import com.fullcycle.ticketsales.domain.order.events.OrderCreated;
import com.fullcycle.ticketsales.domain.order.events.OrderPaid;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade Order - Aggregate Root
 *
 * Representa um pedido de compra de ingresso.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AggregateRoot {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "customer_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String customerId;

    @Column(name = "event_spot_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String eventSpotId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    private Order(String id, String customerId, String eventSpotId, BigDecimal amount) {
        this.id = id;
        this.customerId = customerId;
        this.eventSpotId = eventSpotId;
        this.amount = amount;
        this.status = OrderStatus.PENDING;
    }

    /**
     * Factory method para criar um novo Order.
     */
    public static Order create(String customerId, String eventSpotId, BigDecimal amount) {
        String id = UUID.randomUUID().toString();
        Order order = new Order(id, customerId, eventSpotId, amount);

        // Dispara evento de domínio
        order.addEvent(new OrderCreated(id, customerId, eventSpotId, amount, OrderStatus.PENDING));

        return order;
    }

    /**
     * Marca o pedido como pago.
     * IMPORTANTE: Este método contém lógica de negócio e dispara evento.
     */
    public void pay() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be paid when status is PENDING");
        }

        this.status = OrderStatus.PAID;
        this.addEvent(new OrderPaid(this.id, this.status));
    }

    /**
     * Cancela o pedido.
     */
    public void cancel() {
        if (this.status == OrderStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a paid order");
        }

        this.status = OrderStatus.CANCELLED;
        this.addEvent(new OrderCancelled(this.id, this.status));
    }
}
