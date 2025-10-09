# Spring Boot - Sistema de Venda de Ingressos

Implementação em Spring Boot do sistema de venda de ingressos usando Domain-Driven Design (DDD).

## 📋 Sobre o Projeto

Este projeto demonstra como os padrões DDD implementados no projeto NestJS/TypeScript seriam feitos em Spring Boot/Java.

**Objetivo**: Mostrar as diferenças de implementação entre NestJS e Spring Boot, destacando o que o Spring faz automaticamente "por baixo dos panos".

## 🏗️ Arquitetura

O projeto segue os princípios de DDD (Domain-Driven Design):

- **Domain Layer**: Entidades, agregados, eventos de domínio, repositórios (interfaces)
- **Application Layer**: Services de aplicação, event listeners
- **Interface Layer**: REST controllers

## 🔧 Tecnologias

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** (Hibernate)
- **MySQL**
- **Lombok** (redução de boilerplate)
- **Maven**

## 📁 Estrutura do Projeto

```
src/main/java/com/fullcycle/ticketsales/
├── domain/
│   ├── common/              # AggregateRoot, DomainEvent
│   ├── partner/             # Entidade Partner
│   │   ├── Partner.java
│   │   ├── PartnerRepository.java
│   │   └── events/
│   └── order/               # Entidade Order
│       ├── Order.java
│       ├── OrderRepository.java
│       └── events/
├── application/
│   ├── partner/             # Services e Listeners
│   ├── DomainEventPublisher.java
│   └── TransactionalEventPublisher.java
├── interfaces/
│   └── rest/                # REST Controllers
└── TicketSalesApplication.java
```

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.6+
- MySQL rodando na porta 3306

### Passo a Passo

1. **Clone o repositório** (se ainda não clonou):
```bash
cd fullcycle-pratica-ddd
```

2. **Inicie o MySQL** (usando docker-compose do projeto NestJS):
```bash
docker-compose up -d mysql
```

3. **Navegue até o projeto Spring**:
```bash
cd spring-ticket-sales
```

4. **Compile o projeto**:
```bash
mvn clean install
```

5. **Execute a aplicação**:
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## 🧪 Testando a API

### Criar um parceiro
```bash
curl -X POST http://localhost:8080/api/partners \
  -H "Content-Type: application/json" \
  -d '{"name": "Live Nation Brasil"}'
```

Resposta:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Live Nation Brasil"
}
```

### Listar todos os parceiros
```bash
curl http://localhost:8080/api/partners
```

### Atualizar nome do parceiro
```bash
curl -X PUT http://localhost:8080/api/partners/{id}/name \
  -H "Content-Type: application/json" \
  -d '{"name": "Novo Nome"}'
```

## 📊 Principais Conceitos Demonstrados

### 1. Aggregate Root
```java
@Entity
public class Partner extends AggregateRoot {
    // Entidade raiz do agregado
    // Mantém eventos de domínio
}
```

### 2. Domain Events
```java
public class PartnerCreated implements DomainEvent {
    // Evento disparado quando Partner é criado
}
```

### 3. Repository Pattern com Spring Data JPA
```java
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {
    // Spring gera implementação AUTOMATICAMENTE!
    // Métodos disponíveis: save, findById, findAll, delete, etc
}
```

### 4. Transações Declarativas
```java
@Transactional
public Partner create(String name) {
    // Transação gerenciada automaticamente
    // Commit ao final, rollback em caso de exceção
}
```

### 5. Event Listeners com Garantia Transacional
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handlePartnerCreated(PartnerCreated event) {
    // Executado APÓS commit da transação
    // Garante consistência
}
```

## 🆚 Comparação com NestJS

### O que o Spring faz automaticamente:

| Aspecto | NestJS | Spring Boot |
|---------|--------|-------------|
| **Repositórios CRUD** | ~50 linhas/repositório | 3 linhas (interface) |
| **Transações** | ApplicationService.run() | @Transactional |
| **Unit of Work** | Implementação manual | EntityManager (automático) |
| **Eventos de Domínio** | EventEmitter2 (externo) | ApplicationEventPublisher (nativo) |
| **Injeção de Dependência** | Configuração em módulos | Component scan automático |
| **Mapeamento ORM** | Schemas separados | @Entity (anotações) |

**Resultado**: Spring reduz ~70-85% do código boilerplate.

### Principais Vantagens do Spring

1. ✅ **Repositórios automáticos**: JpaRepository gera implementação
2. ✅ **Transações declarativas**: @Transactional via AOP
3. ✅ **Eventos nativos**: ApplicationEventPublisher integrado
4. ✅ **Dirty checking**: Mudanças detectadas automaticamente
5. ✅ **Menos configuração**: Convention over configuration

### Quando usar cada um

**Use NestJS quando:**
- Equipe domina Node.js/TypeScript
- I/O assíncrono é crítico
- Quer controle explícito de cada detalhe
- Microserviços leves

**Use Spring Boot quando:**
- Quer máxima produtividade
- Aplicações enterprise complexas
- Equipe conhece Java/JVM
- Quer ecossistema maduro

## 📖 Documentação Completa

Para uma comparação detalhada entre NestJS e Spring Boot, leia:

👉 **[COMPARACAO-NESTJS-SPRING.md](./COMPARACAO-NESTJS-SPRING.md)**

Este documento explica:
- Comparação linha por linha
- O que Spring faz "por baixo dos panos"
- Padrões DDD implementados
- Vantagens e desvantagens de cada abordagem

## 🎯 Principais Aprendizados

### No NestJS você precisa:
1. Implementar repositórios manualmente
2. Gerenciar transações explicitamente com ApplicationService
3. Configurar EventEmitter2 para eventos
4. Registrar providers em módulos

### No Spring, o framework faz:
1. Gera repositórios em runtime (Spring Data JPA)
2. Gerencia transações via AOP (@Transactional)
3. Publica eventos nativamente (ApplicationEventPublisher)
4. Descobre e registra beans automaticamente (component scan)

**Resultado**: Muito menos código, mesma funcionalidade.

## 📚 Recursos Adicionais

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)

## 🤝 Contribuindo

Este é um projeto educacional. Sinta-se à vontade para:
- Adicionar mais exemplos
- Melhorar a documentação
- Implementar novos padrões DDD

## 📝 Licença

Este projeto é parte do curso Full Cycle e tem fins educacionais.

---

**Desenvolvido para demonstrar Spring Boot vs NestJS em aplicações DDD**
