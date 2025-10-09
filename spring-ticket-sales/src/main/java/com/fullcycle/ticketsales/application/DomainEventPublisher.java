package com.fullcycle.ticketsales.application;

import com.fullcycle.ticketsales.domain.common.AggregateRoot;
import com.fullcycle.ticketsales.domain.common.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de domínio.
 *
 * No NestJS: DomainEventManager usa EventEmitter2
 * No Spring: Usamos ApplicationEventPublisher nativo do Spring
 *
 * DIFERENÇA CHAVE:
 * - Spring já tem um sistema de eventos integrado (ApplicationEventPublisher)
 * - Não precisa de biblioteca externa como EventEmitter2
 * - Eventos são publicados após commit da transação automaticamente
 */
@Component
@Slf4j
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publica todos os eventos de domínio de um aggregate root.
     *
     * IMPORTANTE: No Spring, com @TransactionalEventListener,
     * podemos garantir que eventos sejam publicados APÓS o commit.
     */
    public void publishEvents(AggregateRoot aggregateRoot) {
        for (DomainEvent event : aggregateRoot.getDomainEvents()) {
            log.debug("Publishing domain event: {}", event.getEventName());
            eventPublisher.publishEvent(event);
        }
        aggregateRoot.clearEvents();
    }
}
