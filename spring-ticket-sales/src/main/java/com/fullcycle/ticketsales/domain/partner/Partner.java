package com.fullcycle.ticketsales.domain.partner;

import com.fullcycle.ticketsales.domain.common.AggregateRoot;
import com.fullcycle.ticketsales.domain.partner.events.PartnerCreated;
import com.fullcycle.ticketsales.domain.partner.events.PartnerNameChanged;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entidade Partner - Aggregate Root
 *
 * No NestJS: Partner é uma classe TypeScript que extends AggregateRoot
 * No Spring: Usamos JPA annotations para persistência automática
 *
 * DIFERENÇA CHAVE:
 * - NestJS: Usa MikroORM com schemas separados para mapeamento
 * - Spring: Usa anotações JPA diretamente na entidade de domínio
 *           (ou pode-se separar em domain model vs persistence model)
 */
@Entity
@Table(name = "partners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA requer construtor vazio
public class Partner extends AggregateRoot {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    private Partner(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Factory method para criar um novo Partner.
     * Equivalente ao método static create() no TypeScript.
     *
     * PADRÃO: Factory Method + Domain Event
     */
    public static Partner create(String name) {
        String id = UUID.randomUUID().toString();
        Partner partner = new Partner(id, name);

        // Dispara evento de domínio
        partner.addEvent(new PartnerCreated(id, name));

        return partner;
    }

    /**
     * Método de negócio para alterar o nome.
     * IMPORTANTE: Dispara evento de domínio.
     */
    public void changeName(String newName) {
        this.name = newName;
        this.addEvent(new PartnerNameChanged(this.id, newName));
    }

    /**
     * NOTA: No Spring com JPA, não precisamos de método toJSON()
     * O Jackson faz serialização automática via getters.
     */
}
