package com.fullcycle.ticketsales.domain.common;

import java.time.Instant;
import java.util.UUID;

/**
 * Interface base para todos os eventos de domínio.
 * No projeto NestJS, isso é feito através de IDomainEvent.
 */
public interface DomainEvent {

    /**
     * Nome do evento (equivalente ao constructor.name no TypeScript)
     */
    String getEventName();

    /**
     * Timestamp de quando o evento ocorreu
     */
    Instant getOccurredOn();

    /**
     * ID único do evento
     */
    UUID getEventId();
}
