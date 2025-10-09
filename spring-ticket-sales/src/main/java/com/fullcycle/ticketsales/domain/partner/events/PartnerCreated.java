package com.fullcycle.ticketsales.domain.partner.events;

import com.fullcycle.ticketsales.domain.common.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de domínio: Partner foi criado
 *
 * No NestJS: Classe simples com constructor
 * No Spring: Usamos record (Java 17+) ou classe com Lombok para imutabilidade
 */
@Getter
public class PartnerCreated implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final String partnerId;
    private final String name;

    public PartnerCreated(String partnerId, String name) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.partnerId = partnerId;
        this.name = name;
    }

    @Override
    public String getEventName() {
        return "PartnerCreated";
    }
}
