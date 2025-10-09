package com.fullcycle.ticketsales.application.partner;

import com.fullcycle.ticketsales.domain.partner.events.PartnerCreated;
import com.fullcycle.ticketsales.domain.partner.events.PartnerNameChanged;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener para eventos de Partner.
 *
 * No NestJS: Handlers registrados no DomainEventManager
 * No Spring: @EventListener ou @TransactionalEventListener
 *
 * DIFERENÇA CHAVE:
 * - @TransactionalEventListener(phase = AFTER_COMMIT) garante que o listener
 *   só é executado APÓS o commit da transação
 * - Isso evita problemas de publicar eventos quando o banco ainda não commitou
 *
 * Fases disponíveis:
 * - BEFORE_COMMIT: Antes do commit
 * - AFTER_COMMIT: Após commit (padrão e mais seguro)
 * - AFTER_ROLLBACK: Após rollback
 * - AFTER_COMPLETION: Após conclusão (commit ou rollback)
 */
@Component
@Slf4j
public class PartnerEventListener {

    /**
     * Processa evento PartnerCreated.
     * Executado APÓS o commit da transação.
     *
     * Aqui você poderia:
     * - Enviar email de boas-vindas
     * - Publicar no RabbitMQ
     * - Atualizar cache
     * - Enviar notificação
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePartnerCreated(PartnerCreated event) {
        log.info("✅ [AFTER_COMMIT] Partner created event received: partnerId={}, name={}",
                event.getPartnerId(), event.getName());

        // Aqui você publicaria no RabbitMQ, por exemplo:
        // rabbitTemplate.convertAndSend("partner-exchange", "partner.created", event);

        // Ou enviaria email:
        // emailService.sendWelcomeEmail(event);
    }

    /**
     * Processa evento PartnerNameChanged.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePartnerNameChanged(PartnerNameChanged event) {
        log.info("✅ [AFTER_COMMIT] Partner name changed: partnerId={}, newName={}",
                event.getPartnerId(), event.getNewName());
    }

    /**
     * Exemplo de listener que executa ANTES do commit.
     * Útil para validações ou preparações.
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommitExample(PartnerCreated event) {
        log.debug("⏳ [BEFORE_COMMIT] Preparing to commit partner creation: {}",
                event.getPartnerId());
    }
}
