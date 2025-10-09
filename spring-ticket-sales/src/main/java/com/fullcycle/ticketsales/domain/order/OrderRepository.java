package com.fullcycle.ticketsales.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para Order.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Busca todos os pedidos de um cliente.
     * Spring Data JPA gera automaticamente a query.
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Busca pedidos por status.
     */
    List<Order> findByStatus(OrderStatus status);
}
