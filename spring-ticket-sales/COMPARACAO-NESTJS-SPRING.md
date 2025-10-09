# Comparação: NestJS/TypeScript vs Spring Boot/Java

## Sistema de Venda de Ingressos - Análise de Implementação DDD

Este documento explica como os padrões e conceitos implementados no projeto NestJS/TypeScript seriam implementados em Spring Boot/Java.

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Principais Diferenças Arquiteturais](#principais-diferenças-arquiteturais)
3. [Comparação Detalhada por Conceito](#comparação-detalhada-por-conceito)
4. [O que o Spring Faz Automaticamente](#o-que-o-spring-faz-automaticamente)
5. [Estrutura de Pastas](#estrutura-de-pastas)
6. [Como Executar](#como-executar)

---

## 🎯 Visão Geral

### Projeto Original (NestJS/TypeScript)
- **Framework**: NestJS (Node.js)
- **ORM**: MikroORM
- **Banco**: MySQL
- **Eventos**: EventEmitter2 (biblioteca externa)
- **Filas**: Bull (Redis)
- **Mensageria**: RabbitMQ
- **Linguagem**: TypeScript

### Projeto Spring (Java)
- **Framework**: Spring Boot
- **ORM**: Spring Data JPA (Hibernate)
- **Banco**: MySQL
- **Eventos**: ApplicationEventPublisher (nativo)
- **Mensageria**: Spring AMQP (RabbitMQ)
- **Linguagem**: Java 17

---

## 🔄 Principais Diferenças Arquiteturais

### 1. Injeção de Dependência

#### NestJS
```typescript
// Usa decorators e módulos
@Module({
  providers: [
    {
      provide: 'IPartnerRepository',
      useFactory: (em: EntityManager) => new PartnerMysqlRepository(em),
      inject: [EntityManager],
    },
    PartnerService,
  ],
})
export class EventsModule {}
```

**Características:**
- Configuração manual via módulos
- Usa factories para criar instâncias
- Precisa registrar cada provider manualmente
- Usa strings como tokens de injeção ('IPartnerRepository')

#### Spring Boot
```java
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {
    // Spring cria implementação automaticamente
}

@Service
public class PartnerApplicationService {
    // Injeção por construtor (recomendado)
    public PartnerApplicationService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }
}
```

**Características:**
- **Configuração por anotações** (@Service, @Repository, @Component)
- **Component scan automático** - Spring encontra os beans sozinho
- **Não precisa de factories** para casos simples
- **Injeção por tipo**, não por string

**✅ Vantagem Spring**: Menos código boilerplate, configuração mais simples.

---

### 2. Persistência / ORM

#### NestJS + MikroORM
```typescript
// 1. Define a entidade de domínio
export class Partner extends AggregateRoot {
  id: PartnerId;
  name: string;
}

// 2. Cria um schema SEPARADO para mapeamento
export const PartnerSchema = new EntitySchema({
  class: Partner,
  tableName: 'partners',
  properties: {
    partner_id: { type: 'uuid', primary: true },
    name: { type: 'string', length: 255 },
  },
});

// 3. Implementa repositório manualmente
export class PartnerMysqlRepository implements IPartnerRepository {
  constructor(private em: EntityManager) {}

  async add(partner: Partner): Promise<void> {
    await this.em.persistAndFlush(partner);
  }

  async findById(id: string): Promise<Partner | null> {
    return await this.em.findOne(Partner, { partner_id: id });
  }

  async findAll(): Promise<Partner[]> {
    return await this.em.find(Partner, {});
  }
}
```

**Características:**
- **Separação** entre entidade de domínio e schema de persistência
- **Implementação manual** de todos os métodos do repositório
- **EntityManager** precisa ser usado explicitamente
- Precisa chamar `persistAndFlush()` ou `flush()` manualmente

#### Spring Boot + JPA
```java
// 1. Entidade com anotações JPA
@Entity
@Table(name = "partners")
public class Partner extends AggregateRoot {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;
}

// 2. Repository interface - Spring gera implementação!
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {
    // Métodos disponíveis AUTOMATICAMENTE:
    // - save(Partner)
    // - findById(String)
    // - findAll()
    // - delete(Partner)
    // - count()
    // - existsById(String)
    // E muitos mais!

    // Queries personalizadas por convenção de nome
    Optional<Partner> findByName(String name);
}
```

**Características:**
- **Anotações JPA** diretamente na entidade (ou pode separar se preferir)
- **Zero implementação** necessária para operações CRUD básicas
- **Spring Data JPA** gera implementação em tempo de execução
- **Queries derivadas** do nome do método
- **Dirty checking** automático - mudanças são detectadas e persistidas

**✅ Vantagem Spring**:
- 90% menos código para repositórios
- Métodos CRUD já prontos
- Queries automáticas por nome do método

---

### 3. Gerenciamento de Transações (Unit of Work)

#### NestJS
```typescript
// 1. Interface do Unit of Work
export interface IUnitOfWork {
  commit(): Promise<void>;
  rollback(): Promise<void>;
  runTransaction<T>(fn: () => Promise<T>): Promise<T>;
  getAggregateRoots(): AggregateRoot[];
}

// 2. Implementação com MikroORM
export class UnitOfWorkMikroOrm implements IUnitOfWork {
  constructor(private em: EntityManager) {}

  async commit(): Promise<void> {
    return this.em.flush();
  }

  async runTransaction<T>(callback: () => Promise<T>): Promise<T> {
    return this.em.transactional(callback);
  }

  getAggregateRoots(): AggregateRoot[] {
    return [
      ...this.em.getUnitOfWork().getPersistStack(),
      ...this.em.getUnitOfWork().getRemoveStack(),
    ];
  }
}

// 3. Application Service coordena manualmente
export class ApplicationService {
  constructor(
    private uow: IUnitOfWork,
    private domainEventManager: DomainEventManager
  ) {}

  async run<T>(callback: () => Promise<T>): Promise<T> {
    await this.start();
    try {
      const result = await callback();
      await this.finish(); // Publica eventos + commit
      return result;
    } catch (e) {
      await this.fail();
      throw e;
    }
  }

  async finish() {
    const aggregateRoots = this.uow.getAggregateRoots();
    // 1. Publica eventos de domínio
    for (const root of aggregateRoots) {
      await this.domainEventManager.publish(root);
    }
    // 2. Commit no banco
    await this.uow.commit();
    // 3. Publica eventos de integração
    for (const root of aggregateRoots) {
      await this.domainEventManager.publishForIntegrationEvent(root);
    }
  }
}

// 4. Uso nos services
export class PartnerService {
  async create(input: { name: string }) {
    return await this.applicationService.run(async () => {
      const partner = Partner.create(input);
      await this.partnerRepo.add(partner);
      return partner;
    });
  }
}
```

**Características:**
- **Gerenciamento manual** de transações
- **ApplicationService** coordena tudo explicitamente
- Precisa chamar `commit()` manualmente
- Controle fino sobre quando eventos são publicados
- Mais código, mas mais controle

#### Spring Boot
```java
// 1. Simplesmente use @Transactional
@Service
public class PartnerApplicationService {

    private final PartnerRepository partnerRepository;
    private final TransactionalEventPublisher eventPublisher;

    @Transactional // <- Isso é tudo que você precisa!
    public Partner create(String name) {
        // 1. Cria entidade
        Partner partner = Partner.create(name);

        // 2. Salva no banco
        Partner saved = partnerRepository.save(partner);

        // 3. Agenda publicação de eventos após commit
        eventPublisher.publishAfterCommit(saved);

        return saved;
    }
}

// 2. Publicador de eventos usa TransactionSynchronization
@Component
public class TransactionalEventPublisher {

    public void publishAfterCommit(AggregateRoot aggregateRoot) {
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    domainEventPublisher.publishEvents(aggregateRoot);
                }
            }
        );
    }
}
```

**Características:**
- **@Transactional** faz TUDO automaticamente:
  - Inicia transação ao entrar no método
  - Commit automático ao finalizar sem exceção
  - Rollback automático se houver exceção
  - Gerencia conexões com banco
- **TransactionSynchronization** garante ordem de execução
- **AOP (Aspect-Oriented Programming)** intercepta chamadas
- Menos código, mais "mágico"

**✅ Vantagem Spring**:
- 95% menos código para gerenciar transações
- Sem necessidade de ApplicationService.run()
- Transações declarativas via anotação

**⚠️ Desvantagem**:
- Menos explícito (pode parecer "mágico")
- Precisa entender proxies do Spring

---

### 4. Sistema de Eventos de Domínio

#### NestJS
```typescript
// 1. DomainEventManager usa EventEmitter2
export class DomainEventManager {
  domainEventsSubscriber: EventEmitter2;

  constructor() {
    this.domainEventsSubscriber = new EventEmitter2({
      wildcard: true,
    });
  }

  register(event: string, handler: any) {
    this.domainEventsSubscriber.on(event, handler);
  }

  async publish(aggregateRoot: AggregateRoot) {
    for (const event of aggregateRoot.events) {
      const eventClassName = event.constructor.name;
      await this.domainEventsSubscriber.emitAsync(eventClassName, event);
    }
  }
}

// 2. Registrar handlers no módulo
@Module({})
export class EventsModule implements OnModuleInit {
  onModuleInit() {
    this.domainEventManager.register('PartnerCreated', async (event) => {
      const handler = await this.moduleRef.resolve(MyHandler);
      await handler.handle(event);
    });
  }
}
```

**Características:**
- **EventEmitter2** biblioteca externa
- **Registro manual** de handlers no módulo
- String-based event names
- Precisa resolver handlers do container manualmente

#### Spring Boot
```java
// 1. Spring tem ApplicationEventPublisher NATIVO
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishEvents(AggregateRoot aggregateRoot) {
        for (DomainEvent event : aggregateRoot.getDomainEvents()) {
            eventPublisher.publishEvent(event);
        }
        aggregateRoot.clearEvents();
    }
}

// 2. Listeners usam anotações
@Component
public class PartnerEventListener {

    // Executa APÓS commit da transação
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePartnerCreated(PartnerCreated event) {
        log.info("Partner created: {}", event.getPartnerId());
        // Enviar email, publicar no RabbitMQ, etc
    }
}
```

**Características:**
- **ApplicationEventPublisher** já vem com Spring
- **@TransactionalEventListener** garante eventos APÓS commit
- **Type-based** dispatch (não precisa de strings)
- **Injeção automática** de listeners
- Controle de fases da transação (BEFORE_COMMIT, AFTER_COMMIT, etc)

**✅ Vantagens Spring**:
- Sistema de eventos nativo, não precisa biblioteca externa
- Listeners registrados automaticamente
- @TransactionalEventListener garante consistência
- Menos código boilerplate

---

### 5. Estrutura de Módulos

#### NestJS
```
apps/mba-ddd-venda-ingresso/
├── src/
│   ├── @core/                        # Camada de domínio
│   │   ├── common/
│   │   │   ├── domain/               # AggregateRoot, Entity, etc
│   │   │   ├── application/          # ApplicationService
│   │   │   └── infra/                # UnitOfWork
│   │   └── events/
│   │       ├── domain/
│   │       │   ├── entities/         # Partner, Event, Order
│   │       │   ├── events/           # Domain events
│   │       │   └── repositories/     # Interfaces
│   │       ├── application/          # Services
│   │       └── infra/                # Implementações
│   ├── database/                     # DatabaseModule
│   ├── application/                  # ApplicationModule
│   ├── domain-events/                # DomainEventsModule
│   ├── events/                       # EventsModule (principal)
│   └── app.module.ts                 # Módulo raiz
```

**Características:**
- Separação física entre domínio, aplicação e infraestrutura
- Schemas separados das entidades
- Cada módulo agrupa providers, controllers, imports, exports

#### Spring Boot
```
spring-ticket-sales/
├── src/main/java/com/fullcycle/ticketsales/
│   ├── domain/                       # Camada de domínio
│   │   ├── common/                   # AggregateRoot, DomainEvent
│   │   ├── partner/
│   │   │   ├── Partner.java          # Entidade (com @Entity JPA)
│   │   │   ├── PartnerRepository.java # Interface Spring Data
│   │   │   └── events/               # PartnerCreated, etc
│   │   └── order/
│   │       ├── Order.java
│   │       ├── OrderRepository.java
│   │       └── events/
│   ├── application/                  # Application Services
│   │   ├── partner/
│   │   │   ├── PartnerApplicationService.java
│   │   │   └── PartnerEventListener.java
│   │   ├── DomainEventPublisher.java
│   │   └── TransactionalEventPublisher.java
│   ├── interfaces/                   # Camada de apresentação
│   │   └── rest/                     # REST Controllers
│   │       └── PartnerController.java
│   └── TicketSalesApplication.java   # Classe principal
```

**Características:**
- Organização por pacotes (domain, application, interfaces)
- Anotações JPA nas entidades de domínio
- Sem necessidade de módulos explícitos (component scan automático)

**✅ Vantagem Spring**:
- Menos arquivos de configuração
- Component scan automático
- Mais simples de navegar

---

## 🤖 O que o Spring Faz Automaticamente

Esta é a parte mais importante para entender! No NestJS, você faz **manualmente** muitas coisas que o Spring faz **automaticamente**.

### 1. Repositórios (CRUD)

#### NestJS: ~50 linhas de código
```typescript
export class PartnerMysqlRepository implements IPartnerRepository {
  constructor(private em: EntityManager) {}

  async add(partner: Partner): Promise<void> {
    await this.em.persistAndFlush(partner);
  }

  async findById(id: string): Promise<Partner | null> {
    return await this.em.findOne(Partner, { partner_id: id });
  }

  async findAll(): Promise<Partner[]> {
    return await this.em.find(Partner, {});
  }

  async delete(partner: Partner): Promise<void> {
    await this.em.removeAndFlush(partner);
  }
}
```

#### Spring: 3 linhas de código!
```java
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {
    // Pronto! Spring gera TUDO automaticamente
}
```

**O que Spring gera automaticamente**:
- save(Partner) - salva ou atualiza
- findById(String) - busca por ID
- findAll() - lista todos
- delete(Partner) - deleta
- count() - conta registros
- existsById(String) - verifica se existe
- E mais ~20 métodos!

---

### 2. Transações

#### NestJS: Gerenciamento manual
```typescript
// Precisa de toda essa estrutura:
export class ApplicationService {
  async run<T>(callback: () => Promise<T>): Promise<T> {
    await this.start();
    try {
      const result = await callback();
      await this.finish();
      return result;
    } catch (e) {
      await this.fail();
      throw e;
    }
  }
}

// E usar assim:
await this.applicationService.run(async () => {
  // código aqui
});
```

#### Spring: Uma anotação
```java
@Transactional
public Partner create(String name) {
    // Transação gerenciada automaticamente!
    // Commit automático ao final
    // Rollback automático se houver exceção
}
```

---

### 3. Validação de Dados

#### NestJS
```typescript
export class CreatePartnerDto {
  @IsNotEmpty()
  @IsString()
  name: string;
}
```

#### Spring
```java
public class CreatePartnerRequest {
    @NotBlank(message = "Partner name is required")
    private String name;
}
```

**Ambos**: Validação automática com anotações ✅

---

### 4. Serialização JSON

#### NestJS
```typescript
export class Partner {
  toJSON() {
    return {
      id: this.id.value,
      name: this.name,
    };
  }
}
```

#### Spring
```java
@Entity
@Getter  // Lombok gera getters
public class Partner {
    private String id;
    private String name;
    // Jackson serializa automaticamente via getters
}
```

**Spring**: Não precisa de método toJSON(), Jackson faz automaticamente.

---

### 5. Injeção de Dependências

#### NestJS: Configuração manual no módulo
```typescript
@Module({
  providers: [
    {
      provide: 'IPartnerRepository',
      useFactory: (em: EntityManager) => new PartnerMysqlRepository(em),
      inject: [EntityManager],
    },
    {
      provide: PartnerService,
      useFactory: (repo, appService) => new PartnerService(repo, appService),
      inject: ['IPartnerRepository', ApplicationService],
    },
  ],
})
export class EventsModule {}
```

#### Spring: Anotações
```java
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {}

@Service
public class PartnerService {
    // Injeção automática por construtor
    public PartnerService(PartnerRepository repo) {}
}
```

**Spring**: Component scan encontra e registra automaticamente.

---

### 6. Sistema de Eventos

#### NestJS: EventEmitter2 (externa) + registro manual
```typescript
// Precisa instalar e configurar EventEmitter2
export class DomainEventManager {
  domainEventsSubscriber: EventEmitter2;

  constructor() {
    this.domainEventsSubscriber = new EventEmitter2({
      wildcard: true,
    });
  }
}

// Registrar handlers manualmente
onModuleInit() {
  this.domainEventManager.register('PartnerCreated', handler);
}
```

#### Spring: ApplicationEventPublisher (nativo)
```java
// Já vem com Spring, só injetar
@Component
public class MyService {
    private final ApplicationEventPublisher publisher;

    public void doSomething() {
        publisher.publishEvent(new MyEvent());
    }
}

// Listeners automáticos
@Component
public class MyListener {
    @EventListener
    public void handle(MyEvent event) {
        // processa
    }
}
```

---

## 📊 Comparação de Linhas de Código

Para implementar as mesmas funcionalidades:

| Funcionalidade | NestJS (TypeScript) | Spring (Java) | Redução |
|----------------|---------------------|---------------|---------|
| Repositório CRUD | ~50 linhas | ~3 linhas | **94%** |
| Gerenciamento de Transações | ~40 linhas | ~1 anotação | **98%** |
| Configuração de Módulos | ~100 linhas | ~0 linhas (automático) | **100%** |
| Sistema de Eventos | ~50 linhas | ~10 linhas (nativo) | **80%** |
| Configuração de DI | ~30 linhas/entidade | ~0 linhas | **100%** |

**Total estimado**: Spring reduz em ~85-90% o código boilerplate.

---

## 🎯 Conceitos que o Spring Gerencia "Por Baixo dos Panos"

### 1. Unit of Work
- **NestJS**: Implementado manualmente com UnitOfWorkMikroOrm
- **Spring**: EntityManager do JPA faz automaticamente
  - Detecta mudanças (dirty checking)
  - Agrupa operações em transação
  - Faz flush automático antes de commit

### 2. Repository Pattern
- **NestJS**: Implementação manual de cada método
- **Spring**: JpaRepository gera em runtime
  - Proxies dinâmicos
  - Reflection e bytecode generation
  - Query derivation a partir do nome do método

### 3. Transações
- **NestJS**: ApplicationService.run() manual
- **Spring**: AOP com @Transactional
  - Proxy intercepta chamadas
  - Inicia transação automaticamente
  - Commit/rollback baseado em exceções

### 4. Eventos de Domínio
- **NestJS**: EventEmitter2 (biblioteca externa)
- **Spring**: ApplicationEventPublisher (nativo)
  - Pub/sub assíncrono
  - @TransactionalEventListener para consistência
  - Integrado com transações

### 5. Connection Pooling
- **NestJS**: Configurado manualmente
- **Spring**: HikariCP automático
  - Pool de conexões configurado
  - Gerenciamento de lifecycle
  - Otimizações automáticas

### 6. Exception Handling
- **NestJS**: Filters manuais
- **Spring**: @ControllerAdvice
  - Tratamento global de exceções
  - Conversão automática para HTTP status

---

## 🏗️ Padrões DDD Implementados

Ambos os projetos implementam os mesmos padrões DDD:

| Padrão | NestJS | Spring | Observação |
|--------|--------|--------|------------|
| **Aggregate Root** | AggregateRoot extends Entity | AggregateRoot + @Entity | Spring adiciona JPA |
| **Domain Events** | IDomainEvent + EventEmitter2 | DomainEvent + ApplicationEventPublisher | Spring nativo |
| **Repository** | Interface + implementação manual | Interface JpaRepository | Spring gera automático |
| **Value Objects** | Classes TypeScript | Classes Java | Similares |
| **Application Service** | ApplicationService.run() | @Transactional | Spring automático |
| **Unit of Work** | UnitOfWorkMikroOrm | EntityManager (JPA) | Spring automático |
| **Domain Events** | Pub/Sub manual | @EventListener | Spring automático |

---

## 📁 Estrutura de Pastas Completa

### Spring Boot (Este Projeto)
```
spring-ticket-sales/
├── pom.xml                                  # Maven dependencies
├── src/
│   ├── main/
│   │   ├── java/com/fullcycle/ticketsales/
│   │   │   ├── domain/
│   │   │   │   ├── common/
│   │   │   │   │   ├── AggregateRoot.java
│   │   │   │   │   └── DomainEvent.java
│   │   │   │   ├── partner/
│   │   │   │   │   ├── Partner.java         # Entidade + @Entity
│   │   │   │   │   ├── PartnerRepository.java
│   │   │   │   │   └── events/
│   │   │   │   │       ├── PartnerCreated.java
│   │   │   │   │       └── PartnerNameChanged.java
│   │   │   │   └── order/
│   │   │   │       ├── Order.java
│   │   │   │       ├── OrderStatus.java
│   │   │   │       ├── OrderRepository.java
│   │   │   │       └── events/
│   │   │   ├── application/
│   │   │   │   ├── partner/
│   │   │   │   │   ├── PartnerApplicationService.java
│   │   │   │   │   ├── CreatePartnerRequest.java
│   │   │   │   │   └── PartnerEventListener.java
│   │   │   │   ├── DomainEventPublisher.java
│   │   │   │   └── TransactionalEventPublisher.java
│   │   │   ├── interfaces/
│   │   │   │   └── rest/
│   │   │   │       └── PartnerController.java
│   │   │   └── TicketSalesApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── COMPARACAO-NESTJS-SPRING.md             # Este documento
```

---

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.6+
- MySQL rodando (usar docker-compose do projeto NestJS)

### Passos

1. **Subir infraestrutura** (MySQL, Redis, RabbitMQ):
```bash
# Na raiz do projeto NestJS
docker-compose up -d
```

2. **Navegar até o projeto Spring**:
```bash
cd spring-ticket-sales
```

3. **Compilar projeto**:
```bash
mvn clean install
```

4. **Executar aplicação**:
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Testando a API

#### Criar um parceiro:
```bash
curl -X POST http://localhost:8080/api/partners \
  -H "Content-Type: application/json" \
  -d '{"name": "Live Nation Brasil"}'
```

#### Listar parceiros:
```bash
curl http://localhost:8080/api/partners
```

#### Atualizar nome:
```bash
curl -X PUT http://localhost:8080/api/partners/{id}/name \
  -H "Content-Type: application/json" \
  -d '{"name": "Novo Nome"}'
```

### Logs de Eventos

Ao criar um parceiro, você verá nos logs:

```
✅ [AFTER_COMMIT] Partner created event received: partnerId=..., name=Live Nation Brasil
```

Isso demonstra que:
1. Transação foi commitada
2. Evento foi publicado APÓS commit
3. Listener processou o evento

---

## 💡 Principais Aprendizados

### O que NestJS faz que você precisa implementar manualmente:
1. ✍️ **Repositórios**: Implementar todos os métodos CRUD
2. ✍️ **Transações**: ApplicationService.run() para coordenar
3. ✍️ **Schemas**: Separar entidade de domínio e schema de persistência
4. ✍️ **Unit of Work**: Implementar interface e lógica de commit/rollback
5. ✍️ **Eventos**: Instalar EventEmitter2 e registrar handlers manualmente
6. ✍️ **Módulos**: Configurar providers, imports, exports em cada módulo

### O que Spring Boot faz automaticamente:
1. ✅ **Repositórios**: JpaRepository gera implementação em runtime
2. ✅ **Transações**: @Transactional gerencia tudo via AOP
3. ✅ **Persistência**: @Entity JPA mapeia diretamente
4. ✅ **Unit of Work**: EntityManager faz dirty checking automático
5. ✅ **Eventos**: ApplicationEventPublisher nativo do Spring
6. ✅ **Injeção**: Component scan e injeção automática

### Quando usar cada um:

**Use NestJS/TypeScript quando**:
- Equipe já conhece Node.js/JavaScript
- Precisa de alta performance em I/O assíncrono
- Quer controle fino sobre cada aspecto
- Microserviços leves e rápidos
- Prototipagem rápida com TypeScript

**Use Spring Boot/Java quando**:
- Precisa de ecossistema maduro e estável
- Quer menos código boilerplate
- Equipe conhece Java/JVM
- Aplicações enterprise complexas
- Quer convenção sobre configuração
- Precisa de ferramentas prontas

---

## 📚 Conceitos Fundamentais

### 1. Aggregate Root
**Igual em ambos**: Classe base que mantém lista de eventos de domínio.

### 2. Domain Events
**Diferença**: NestJS usa EventEmitter2, Spring usa ApplicationEventPublisher nativo.

### 3. Repository Pattern
**Grande diferença**:
- NestJS: implementação manual
- Spring: geração automática via Spring Data JPA

### 4. Unit of Work
**Grande diferença**:
- NestJS: implementado manualmente com UnitOfWorkMikroOrm
- Spring: EntityManager do JPA faz automaticamente (dirty checking)

### 5. Application Service
**Grande diferença**:
- NestJS: ApplicationService.run() coordena tudo explicitamente
- Spring: @Transactional + AOP faz automaticamente

### 6. Transactional Events
**Grande diferença**:
- NestJS: ApplicationService.finish() publica após commit manualmente
- Spring: @TransactionalEventListener(phase = AFTER_COMMIT) garante automaticamente

---

## 🎓 Conclusão

### Filosofia de Design

**NestJS**: "Explicit is better than implicit"
- Você escreve cada passo
- Mais controle, mais código
- Sabe exatamente o que está acontecendo

**Spring Boot**: "Convention over configuration"
- Framework faz o trabalho pesado
- Menos código, mais produtividade
- Precisa entender "a mágica" do Spring

### Produtividade

Para implementar as mesmas funcionalidades de domínio:
- **NestJS**: ~1000 linhas de código
- **Spring**: ~300 linhas de código

**Spring reduz ~70% do código** através de:
- Spring Data JPA (repositories)
- @Transactional (transações)
- ApplicationEventPublisher (eventos)
- Component scanning (DI)
- JPA EntityManager (Unit of Work)

### Curva de Aprendizado

**NestJS**:
- ✅ Mais fácil se já conhece Node.js
- ✅ Código mais explícito e didático
- ⚠️ Precisa aprender MikroORM
- ⚠️ Precisa implementar muita coisa manualmente

**Spring**:
- ⚠️ Curva de aprendizado inicial maior
- ⚠️ Precisa entender "mágica" do Spring (proxies, AOP)
- ✅ Depois que aprende, extremamente produtivo
- ✅ Ecossistema enorme de soluções prontas

### Recomendação

- **Aprendizado de DDD**: NestJS é melhor (mais explícito)
- **Produtividade**: Spring é melhor (menos código)
- **Performance I/O**: NestJS é melhor (Node.js assíncrono)
- **Aplicações Enterprise**: Spring é melhor (ecossistema maduro)

---

## 📖 Referências

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [NestJS Documentation](https://docs.nestjs.com/)
- [Domain-Driven Design (Eric Evans)](https://www.domainlanguage.com/ddd/)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

---

**Autor**: Claude Code
**Data**: Janeiro 2025
**Projeto**: Comparação NestJS vs Spring Boot - Sistema de Venda de Ingressos DDD
