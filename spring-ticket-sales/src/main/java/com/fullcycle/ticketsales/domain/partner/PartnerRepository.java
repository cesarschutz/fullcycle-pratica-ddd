package com.fullcycle.ticketsales.domain.partner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para Partner.
 *
 * No NestJS: Interface IPartnerRepository + implementação PartnerMysqlRepository
 * No Spring: JpaRepository já fornece implementação automática!
 *
 * DIFERENÇA CHAVE:
 * - NestJS: Precisa implementar manualmente todos os métodos do repositório
 * - Spring Data JPA: Gera implementação automaticamente baseado em convenções
 *
 * Métodos disponíveis automaticamente:
 * - save(Partner) -> salva ou atualiza
 * - findById(String) -> busca por ID
 * - findAll() -> lista todos
 * - delete(Partner) -> deleta
 * - existsById(String) -> verifica existência
 * - count() -> conta registros
 * - E muito mais!
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {

    /**
     * Spring Data JPA permite criar queries customizadas apenas declarando métodos!
     * Exemplo: findByName irá gerar automaticamente:
     * SELECT p FROM Partner p WHERE p.name = :name
     */
    Optional<Partner> findByName(String name);

    /**
     * Podemos usar @Query para queries mais complexas:
     * @Query("SELECT p FROM Partner p WHERE p.name LIKE %:name%")
     * List<Partner> searchByName(@Param("name") String name);
     */
}
