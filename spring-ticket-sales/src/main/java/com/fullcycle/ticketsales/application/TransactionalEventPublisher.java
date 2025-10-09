package com.fullcycle.ticketsales.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.fullcycle.ticketsales.domain.common.AggregateRoot;

/**
 * Gerenciador de publicação de eventos após commit de transação.
 *
 * No NestJS: ApplicationService.finish() publica eventos manualmente
 * No Spring: Podemos usar TransactionSynchronization para garantir
 *           que eventos sejam publicados APÓS o commit
 *
 * PADRÃO: Pub/Sub com garantia de consistência transacional
 */
@Component
@Slf4j
public class TransactionalEventPublisher {

    @PersistenceContext
    private EntityManager entityManager;

    private final DomainEventPublisher domainEventPublisher;

    public TransactionalEventPublisher(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * Agenda a publicação de eventos para APÓS o commit da transação.
     *
     * Isso é equivalente ao que o ApplicationService faz no NestJS:
     * 1. Executa operação de negócio
     * 2. Faz commit no banco
     * 3. Publica eventos
     */
    public void publishAfterCommit(AggregateRoot aggregateRoot) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("No active transaction, publishing events immediately");
            domainEventPublisher.publishEvents(aggregateRoot);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.debug("Transaction committed, publishing events for aggregate");
                    domainEventPublisher.publishEvents(aggregateRoot);
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        log.warn("Transaction rolled back, clearing events");
                        aggregateRoot.clearEvents();
                    }
                }
            }
        );
    }
}
