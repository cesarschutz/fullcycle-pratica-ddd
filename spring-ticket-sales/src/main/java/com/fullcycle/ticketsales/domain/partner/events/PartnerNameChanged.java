package com.fullcycle.ticketsales.domain.partner.events;

import com.fullcycle.ticketsales.domain.common.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PartnerNameChanged implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final String partnerId;
    private final String newName;

    public PartnerNameChanged(String partnerId, String newName) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.partnerId = partnerId;
        this.newName = newName;
    }

    @Override
    public String getEventName() {
        return "PartnerNameChanged";
    }
}
