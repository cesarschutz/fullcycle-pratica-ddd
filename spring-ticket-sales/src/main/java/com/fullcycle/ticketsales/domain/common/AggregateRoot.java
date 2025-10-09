package com.fullcycle.ticketsales.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe base para Aggregate Roots no padrão DDD.
 *
 * No projeto NestJS, a classe AggregateRoot mantém um Set<IDomainEvent>.
 * Aqui, usamos uma List por ser mais idiomático em Java.
 *
 * Esta classe é responsável por:
 * - Manter a lista de eventos de domínio gerados
 * - Fornecer métodos para adicionar e limpar eventos
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Adiciona um evento de domínio à lista.
     * Equivalente ao método addEvent() no TypeScript.
     */
    protected void addEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Retorna uma lista imutável dos eventos de domínio.
     * Isso previne modificações externas na lista.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Limpa todos os eventos de domínio.
     * Equivalente ao método clearEvents() no TypeScript.
     */
    public void clearEvents() {
        this.domainEvents.clear();
    }

    /**
     * Verifica se há eventos pendentes.
     */
    public boolean hasEvents() {
        return !domainEvents.isEmpty();
    }
}
