# Estrutura Completa do Projeto Spring

## 📦 Arquivos Criados

### 📄 Configuração (3 arquivos)
```
✅ pom.xml                              # Dependências Maven
✅ src/main/resources/application.properties
✅ BUILD-INSTRUCTIONS.md                # Instruções de build
```

### 🏛️ Camada de Domínio (11 arquivos)

#### Classes Base
```
✅ domain/common/AggregateRoot.java      # Classe base para agregados
✅ domain/common/DomainEvent.java        # Interface para eventos
```

#### Agregado Partner (5 arquivos)
```
✅ domain/partner/Partner.java           # Entidade Partner + @Entity JPA
✅ domain/partner/PartnerRepository.java # Interface Spring Data JPA
✅ domain/partner/events/PartnerCreated.java
✅ domain/partner/events/PartnerNameChanged.java
```

#### Agregado Order (5 arquivos)
```
✅ domain/order/Order.java               # Entidade Order + @Entity JPA
✅ domain/order/OrderRepository.java     # Interface Spring Data JPA
✅ domain/order/OrderStatus.java         # Enum de status
✅ domain/order/events/OrderCreated.java
✅ domain/order/events/OrderPaid.java
✅ domain/order/events/OrderCancelled.java
```

### 🔧 Camada de Aplicação (6 arquivos)

#### Gerenciamento de Eventos
```
✅ application/DomainEventPublisher.java           # Publica eventos
✅ application/TransactionalEventPublisher.java    # Sincronização com transações
```

#### Partner - Application Services
```
✅ application/partner/PartnerApplicationService.java  # Service principal
✅ application/partner/CreatePartnerRequest.java       # DTO
✅ application/partner/PartnerEventListener.java       # Event listener
```

### 🌐 Camada de Interface (1 arquivo)
```
✅ interfaces/rest/PartnerController.java         # REST Controller
```

### 🚀 Classe Principal (1 arquivo)
```
✅ TicketSalesApplication.java                    # Main class Spring Boot
```

### 📚 Documentação (3 arquivos)
```
✅ README.md                                      # Documentação principal
✅ COMPARACAO-NESTJS-SPRING.md                   # Comparação detalhada
✅ BUILD-INSTRUCTIONS.md                          # Instruções de build
```

---

## 📊 Estatísticas

- **Total de arquivos Java**: 19
- **Total de linhas de código**: ~1.200 linhas (com comentários)
- **Arquivos de documentação**: 3 (>60KB de documentação)
- **Camadas arquiteturais**: 3 (Domain, Application, Interface)
- **Agregados implementados**: 2 (Partner, Order)
- **Eventos de domínio**: 6
- **Repositories**: 2 (gerados automaticamente pelo Spring)
- **Controllers REST**: 1
- **Application Services**: 1

---

## 🎯 Principais Padrões Implementados

### 1. Domain-Driven Design (DDD)
- ✅ Aggregate Roots (Partner, Order)
- ✅ Domain Events (6 eventos)
- ✅ Repository Pattern (interfaces Spring Data)
- ✅ Value Objects (potencial para expandir)
- ✅ Application Services

### 2. Event-Driven Architecture
- ✅ Domain Events
- ✅ Event Listeners
- ✅ Transactional Events (@TransactionalEventListener)
- ✅ Event Publisher

### 3. Arquitetura em Camadas
- ✅ Domain Layer (entidades, eventos, repositórios)
- ✅ Application Layer (services, listeners)
- ✅ Interface Layer (controllers REST)

### 4. Padrões Spring Boot
- ✅ Dependency Injection (via constructor)
- ✅ Component Scanning
- ✅ Transaction Management (@Transactional)
- ✅ Spring Data JPA (repositories automáticos)
- ✅ Bean Validation (jakarta.validation)
- ✅ AOP (Aspect-Oriented Programming)

---

## 🔍 Comparação com Projeto NestJS

### Arquivos NestJS vs Spring

| Componente | NestJS (TypeScript) | Spring (Java) | Redução |
|------------|---------------------|---------------|---------|
| **Repository Implementation** | ~50 linhas | 3 linhas (interface) | 94% |
| **Application Service** | ~40 linhas (+ ApplicationService.run) | ~50 linhas (@Transactional) | 0% (similar) |
| **Module Configuration** | ~100 linhas (providers, imports) | 0 linhas (component scan) | 100% |
| **Unit of Work** | ~40 linhas (implementação) | 0 linhas (JPA automático) | 100% |
| **Event System** | ~50 linhas (EventEmitter2) | ~30 linhas (nativo) | 40% |
| **ORM Schemas** | ~50 linhas (EntitySchema) | 0 linhas (anotações JPA) | 100% |

### Total de Código Boilerplate

- **NestJS**: ~800 linhas de código de infraestrutura
- **Spring**: ~100 linhas de código de infraestrutura
- **Redução**: ~87% menos boilerplate

---

## 🏗️ Estrutura de Diretórios Completa

```
spring-ticket-sales/
├── pom.xml
├── README.md
├── COMPARACAO-NESTJS-SPRING.md
├── BUILD-INSTRUCTIONS.md
├── ESTRUTURA-PROJETO.md (este arquivo)
│
├── src/
│   ├── main/
│   │   ├── java/com/fullcycle/ticketsales/
│   │   │   │
│   │   │   ├── TicketSalesApplication.java        [MAIN]
│   │   │   │
│   │   │   ├── domain/                             [CAMADA DE DOMÍNIO]
│   │   │   │   ├── common/
│   │   │   │   │   ├── AggregateRoot.java         # Classe base
│   │   │   │   │   └── DomainEvent.java           # Interface de eventos
│   │   │   │   │
│   │   │   │   ├── partner/                        [AGREGADO PARTNER]
│   │   │   │   │   ├── Partner.java               # Entidade raiz
│   │   │   │   │   ├── PartnerRepository.java     # Repository interface
│   │   │   │   │   └── events/
│   │   │   │   │       ├── PartnerCreated.java
│   │   │   │   │       └── PartnerNameChanged.java
│   │   │   │   │
│   │   │   │   └── order/                          [AGREGADO ORDER]
│   │   │   │       ├── Order.java                 # Entidade raiz
│   │   │   │       ├── OrderStatus.java           # Enum
│   │   │   │       ├── OrderRepository.java       # Repository interface
│   │   │   │       └── events/
│   │   │   │           ├── OrderCreated.java
│   │   │   │           ├── OrderPaid.java
│   │   │   │           └── OrderCancelled.java
│   │   │   │
│   │   │   ├── application/                        [CAMADA DE APLICAÇÃO]
│   │   │   │   ├── DomainEventPublisher.java
│   │   │   │   ├── TransactionalEventPublisher.java
│   │   │   │   └── partner/
│   │   │   │       ├── PartnerApplicationService.java
│   │   │   │       ├── CreatePartnerRequest.java
│   │   │   │       └── PartnerEventListener.java
│   │   │   │
│   │   │   └── interfaces/                         [CAMADA DE INTERFACE]
│   │   │       └── rest/
│   │   │           └── PartnerController.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/fullcycle/ticketsales/
│
└── target/ (gerado após build)
    ├── classes/
    └── spring-ticket-sales-1.0.0.jar
```

---

## 🎓 Conceitos-Chave Implementados

### 1. Aggregate Root
```java
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    // Mantém eventos de domínio até serem publicados
}
```

### 2. Domain Events
```java
public class PartnerCreated implements DomainEvent {
    // Evento imutável disparado quando Partner é criado
}
```

### 3. Repository Pattern (Spring Data JPA)
```java
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {
    // Spring gera implementação AUTOMATICAMENTE
}
```

### 4. Application Service com @Transactional
```java
@Service
public class PartnerApplicationService {
    @Transactional  // Gerencia transação automaticamente
    public Partner create(String name) { ... }
}
```

### 5. Transactional Event Listener
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handlePartnerCreated(PartnerCreated event) {
    // Executado APÓS commit da transação
}
```

---

## 🚀 Como Usar Este Projeto

### 1. Para Aprender DDD com Spring
1. Leia `README.md` para visão geral
2. Explore código começando por `Partner.java`
3. Veja como eventos funcionam em `PartnerEventListener.java`
4. Entenda transações em `PartnerApplicationService.java`

### 2. Para Comparar com NestJS
1. Leia `COMPARACAO-NESTJS-SPRING.md`
2. Compare lado a lado:
   - Repository: NestJS manual vs Spring automático
   - Transações: ApplicationService.run() vs @Transactional
   - Eventos: EventEmitter2 vs ApplicationEventPublisher

### 3. Para Implementar Novas Features
1. Crie nova entidade em `domain/`
2. Crie repository interface (Spring gera implementação)
3. Crie application service com @Transactional
4. Crie controller REST
5. Adicione event listeners se necessário

---

## 📖 Documentos Disponíveis

1. **README.md** (principal)
   - Visão geral do projeto
   - Como executar
   - Exemplos de API
   - Conceitos demonstrados

2. **COMPARACAO-NESTJS-SPRING.md** (detalhado - 28KB)
   - Comparação linha por linha
   - O que Spring faz automaticamente
   - Tabelas comparativas
   - Exemplos de código lado a lado
   - Filosofias de design
   - Quando usar cada framework

3. **BUILD-INSTRUCTIONS.md** (prático)
   - Como instalar pré-requisitos
   - Como compilar projeto
   - Como executar
   - Troubleshooting

4. **ESTRUTURA-PROJETO.md** (este arquivo)
   - Lista completa de arquivos
   - Estatísticas do projeto
   - Estrutura de diretórios
   - Guia de navegação

---

## 🎯 Próximos Passos Sugeridos

Para expandir este projeto, você pode:

### 1. Adicionar Mais Agregados
- ✅ Partner (implementado)
- ✅ Order (implementado)
- ⬜ Event (evento/show)
- ⬜ Customer (cliente)
- ⬜ SpotReservation (reserva de lugar)

### 2. Implementar Casos de Uso Completos
- ⬜ Criar evento completo (com seções e spots)
- ⬜ Processo de compra de ingresso
- ⬜ Pagamento integrado
- ⬜ Cancelamento de pedido

### 3. Adicionar Testes
- ⬜ Testes unitários (JUnit 5)
- ⬜ Testes de integração (Spring Boot Test)
- ⬜ Testes de API (MockMvc)

### 4. Integração com RabbitMQ
- ⬜ Publicar eventos de integração no RabbitMQ
- ⬜ Consumir eventos de outras aplicações
- ⬜ Dead letter queue

### 5. Observabilidade
- ⬜ Logging estruturado
- ⬜ Métricas (Micrometer)
- ⬜ Health checks
- ⬜ Distributed tracing

---

## 🤔 Dúvidas Frequentes

### Por que algumas entidades têm @Entity e outras não?
Apenas as entidades que serão persistidas (Partner, Order) têm @Entity.
Classes abstratas (AggregateRoot) e interfaces (DomainEvent) não precisam.

### Por que não tem método save() no repository?
Spring Data JPA já fornece `save()` automaticamente via JpaRepository.
Você só precisa declarar a interface!

### Como o Spring sabe onde procurar os beans?
`@SpringBootApplication` inclui `@ComponentScan` que escaneia automaticamente
todas as classes no pacote e subpacotes.

### Preciso chamar commit() explicitamente?
Não! @Transactional faz commit automático ao final do método.
Só faz rollback se houver exceção.

### Como adicionar um novo endpoint?
1. Adicione método no Controller com @GetMapping/@PostMapping
2. Chame o Application Service
3. Pronto! Spring cuida do resto.

---

## 📝 Notas Finais

Este projeto foi criado como exemplo educacional para demonstrar:

1. ✅ Como implementar DDD com Spring Boot
2. ✅ Diferenças entre NestJS e Spring Boot
3. ✅ O que o Spring faz "automaticamente"
4. ✅ Padrões de arquitetura limpa

**Foco**: Clareza e didática sobre produção-ready.

**Objetivo alcançado**: Mostrar que Spring Boot reduz significativamente
o código boilerplate (~85-90%) em comparação com NestJS, mantendo os
mesmos princípios de DDD e arquitetura limpa.

---

**Desenvolvido para demonstração educacional**
**Projeto: Spring Boot vs NestJS - Sistema de Venda de Ingressos DDD**
