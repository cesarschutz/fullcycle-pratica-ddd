package com.fullcycle.ticketsales.application.partner;

import com.fullcycle.ticketsales.application.TransactionalEventPublisher;
import com.fullcycle.ticketsales.domain.partner.Partner;
import com.fullcycle.ticketsales.domain.partner.PartnerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service para Partner.
 *
 * No NestJS: PartnerService com ApplicationService.run()
 * No Spring: @Service + @Transactional
 *
 * DIFERENÇA CHAVE:
 * - NestJS: Precisa chamar applicationService.run() explicitamente
 * - Spring: @Transactional gerencia transações automaticamente via AOP
 *
 * O Spring fornece:
 * - Transação automática (não precisa de begin/commit manual)
 * - Rollback automático em caso de exceção
 * - Gestão de conexões com o banco
 */
@Service
@Slf4j
public class PartnerApplicationService {

    private final PartnerRepository partnerRepository;
    private final TransactionalEventPublisher eventPublisher;

    public PartnerApplicationService(PartnerRepository partnerRepository,
                                    TransactionalEventPublisher eventPublisher) {
        this.partnerRepository = partnerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Lista todos os parceiros.
     * Operação de LEITURA - não precisa de transação de escrita.
     */
    @Transactional(readOnly = true)
    public List<Partner> listAll() {
        return partnerRepository.findAll();
    }

    /**
     * Cria um novo parceiro.
     *
     * @Transactional garante que:
     * 1. Uma transação seja iniciada
     * 2. O save() seja executado
     * 3. Se houver erro, rollback automático
     * 4. Se sucesso, commit automático
     */
    @Transactional
    public Partner create(String name) {
        log.info("Creating partner with name: {}", name);

        // 1. Cria a entidade (dispara evento PartnerCreated)
        Partner partner = Partner.create(name);

        // 2. Salva no banco
        Partner saved = partnerRepository.save(partner);

        // 3. Publica eventos após commit
        eventPublisher.publishAfterCommit(saved);

        return saved;
    }

    /**
     * Atualiza nome do parceiro.
     */
    @Transactional
    public Partner updateName(String id, String newName) {
        log.info("Updating partner {} name to: {}", id, newName);

        Partner partner = partnerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Partner not found: " + id));

        // Chama método de negócio (dispara evento PartnerNameChanged)
        partner.changeName(newName);

        // JPA detecta mudanças automaticamente (dirty checking)
        // Não precisa chamar save() explicitamente!
        // Mas vamos chamar para deixar explícito
        Partner updated = partnerRepository.save(partner);

        eventPublisher.publishAfterCommit(updated);

        return updated;
    }
}
