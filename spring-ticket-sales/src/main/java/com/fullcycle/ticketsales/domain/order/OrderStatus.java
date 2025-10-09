package com.fullcycle.ticketsales.domain.order;

/**
 * Enum para status do pedido.
 *
 * No NestJS: enum OrderStatus { PENDING, PAID, CANCELLED }
 * No Spring: enum Java padrão
 */
public enum OrderStatus {
    PENDING,
    PAID,
    CANCELLED
}
