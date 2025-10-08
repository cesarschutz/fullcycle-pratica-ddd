# Arquitetura NestJS - Sistema de Venda de Ingressos (DDD)

## Índice
1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [O que é NestJS?](#o-que-é-nestjs)
3. [Conceitos Fundamentais](#conceitos-fundamentais)
4. [Arquitetura do Projeto](#arquitetura-do-projeto)
5. [Módulos Detalhados](#módulos-detalhados)
6. [Fluxos de Negócio](#fluxos-de-negócio)
7. [Padrões de Projeto Utilizados](#padrões-de-projeto-utilizados)
8. [Como Executar](#como-executar)

---

## 📌 Visão Geral do Projeto

Este projeto implementa um **sistema de venda de ingressos** utilizando **Domain-Driven Design (DDD)** e **Event-Driven Architecture (EDA)**. O sistema é composto por duas aplicações NestJS:

### Aplicações do Sistema

#### 1. **mba-ddd-venda-ingresso** (Aplicação Principal)
- **Porta:** 3000
- **Função:** API REST para gerenciamento de eventos, parceiros, clientes e pedidos
- **Responsabilidades:**
  - Gerenciar parceiros (organizadores de eventos)
  - Criar e publicar eventos
  - Gerenciar seções e spots (lugares) dos eventos
  - Processar pedidos de compra de ingressos
  - Publicar eventos de integração para outras aplicações

#### 2. **emails** (Serviço de Notificações)
- **Porta:** 3001
- **Função:** Consumir eventos e enviar notificações
- **Responsabilidades:**
  - Escutar eventos do RabbitMQ
  - Processar eventos de criação de parceiros, pedidos, etc.
  - Enviar emails de notificação (simulado)

### Tecnologias Principais
- **NestJS**: Framework Node.js com TypeScript
- **MikroORM**: ORM para comunicação com MySQL
- **MySQL**: Banco de dados relacional
- **Redis + Bull**: Sistema de filas para processamento assíncrono
- **RabbitMQ**: Message broker para comunicação entre aplicações
- **TypeScript**: Linguagem tipada

---

## 📚 O que é NestJS?

NestJS é um framework para construir aplicações do lado do servidor (backend) com Node.js e TypeScript. Ele é organizado em **módulos**, que funcionam como blocos de construção para sua aplicação.

### Analogia da Cidade
Pense no NestJS como uma cidade bem organizada:
- **Módulos** → **Bairros** da cidade (cada um com sua função específica)
- **Controllers** → **Portarias** que recebem visitantes (requisições HTTP)
- **Services** → **Escritórios** onde o trabalho real acontece
- **Providers** → **Funcionários** especializados em tarefas específicas
- **Repositories** → **Bibliotecários** que sabem onde buscar e guardar informações

---

## 🧩 Conceitos Fundamentais

### 1️⃣ Decorators (Decoradores)

Decorators são "etiquetas especiais" que você coloca em classes, métodos ou propriedades para adicionar comportamentos especiais. São como placas de sinalização no código.

**Principais Decorators do NestJS:**

```typescript
@Module()       // Marca uma classe como módulo
@Controller()   // Marca uma classe como controlador de rotas
@Injectable()   // Marca uma classe como injetável (pode ser usada em outros lugares)
@Get()         // Define uma rota HTTP GET
@Post()        // Define uma rota HTTP POST
@Put()         // Define uma rota HTTP PUT
@Delete()      // Define uma rota HTTP DELETE
@Body()        // Extrai o corpo da requisição
@Param()       // Extrai parâmetros da URL
@Global()      // Torna um módulo disponível globalmente
```

**Exemplo Prático:**
```typescript
@Injectable()  // 👈 Esta classe pode ser injetada em outras classes
export class PartnerService {
  constructor(private partnerRepo: IPartnerRepository) {}

  async create(name: string) {
    // Lógica de criação
  }
}

@Controller('partners')  // 👈 Responde em /partners
export class PartnersController {
  constructor(private partnerService: PartnerService) {}

  @Get()  // 👈 Responde a GET /partners
  list() {
    return this.partnerService.list();
  }

  @Post()  // 👈 Responde a POST /partners
  create(@Body() body: { name: string }) {  // 👈 Pega o JSON do body
    return this.partnerService.create(body);
  }
}
```

### 2️⃣ Module (Módulo)

Um módulo é um **container organizacional** que agrupa código relacionado.

**Estrutura de um Módulo:**
```typescript
@Module({
  imports: [       // 📦 Módulos que este módulo precisa usar
    DatabaseModule,
    OtherModule
  ],
  controllers: [   // 🎮 Controladores que recebem requisições HTTP
    UsersController
  ],
  providers: [     // 🔧 Serviços, repositórios, factories
    UsersService,
    UsersRepository
  ],
  exports: [       // 📤 O que este módulo disponibiliza para outros
    UsersService
  ]
})
export class UsersModule {}
```

**Explicando cada parte:**
- **imports**: Outros módulos necessários (como importar bibliotecas)
- **controllers**: Quem recebe as requisições HTTP
- **providers**: Quem faz o trabalho (services, repositories)
- **exports**: O que será compartilhado com outros módulos

### 3️⃣ Controller (Controlador)

Controladores **recebem requisições HTTP** e decidem o que fazer com elas.

**Analogia:** Recepcionista de hotel que direciona você para o lugar certo.

```typescript
@Controller('events')
export class EventsController {
  constructor(private eventService: EventService) {}

  @Get()  // GET /events
  list() {
    return this.eventService.findEvents();
  }

  @Post()  // POST /events
  create(@Body() body: EventDto) {
    return this.eventService.create(body);
  }

  @Put(':id/publish')  // PUT /events/:id/publish
  publish(@Param('id') id: string) {
    return this.eventService.publishAll({ event_id: id });
  }
}
```

### 4️⃣ Service (Serviço)

Services contêm a **lógica de negócio**. Fazem validações, cálculos, coordenam operações complexas.

**Analogia:** Gerente de banco que executa todo o processo.

```typescript
@Injectable()
export class PartnerService {
  constructor(
    private partnerRepo: IPartnerRepository,
    private appService: ApplicationService
  ) {}

  async create(input: { name: string }) {
    return await this.appService.run(async () => {
      const partner = Partner.create(input);  // Cria entidade
      await this.partnerRepo.add(partner);    // Salva no banco
      return partner;
    });
  }
}
```

### 5️⃣ Dependency Injection (Injeção de Dependência)

O NestJS **fornece automaticamente** as dependências que uma classe precisa.

**Analogia:** Como um chef que encontra todos os ingredientes já preparados na bancada.

```typescript
export class OrderService {
  constructor(
    // 👇 NestJS injeta automaticamente todas essas dependências
    private orderRepo: IOrderRepository,
    private customerRepo: ICustomerRepository,
    private eventRepo: IEventRepository,
    private uow: IUnitOfWork,
    private paymentGateway: PaymentGateway
  ) {}
}
```

### 6️⃣ Repository (Repositório)

Repositories **conversam com o banco de dados** para salvar, buscar, atualizar ou deletar dados.

**Analogia:** Bibliotecário que sabe exatamente onde está cada livro.

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
}
```

---

## 🏗️ Arquitetura do Projeto

### Estrutura de Pastas

```
fullcycle-pratica-ddd/
├── apps/
│   ├── mba-ddd-venda-ingresso/     # 📱 Aplicação Principal (API REST)
│   │   └── src/
│   │       ├── @core/               # 🎯 Camada de Domínio (DDD)
│   │       │   ├── common/          # Código compartilhado
│   │       │   │   ├── application/ # ApplicationService, interfaces
│   │       │   │   ├── domain/      # DomainEventManager, AggregateRoot
│   │       │   │   └── infra/       # UnitOfWork, implementações
│   │       │   ├── events/          # Contexto de Eventos (Bounded Context)
│   │       │   │   ├── domain/      # Entidades, Value Objects, Eventos
│   │       │   │   │   ├── entities/           # Partner, Event, Customer, Order
│   │       │   │   │   ├── events/             # Eventos de domínio e integração
│   │       │   │   │   └── repositories/       # Interfaces dos repositórios
│   │       │   │   ├── application/ # Services (PartnerService, EventService)
│   │       │   │   └── infra/       # Implementações (Repositories, Schemas)
│   │       │   └── stored-events/   # Armazena eventos para auditoria
│   │       ├── application/         # 🔧 ApplicationModule
│   │       ├── database/            # 💾 DatabaseModule (MikroORM config)
│   │       ├── domain-events/       # 📢 DomainEventsModule
│   │       ├── events/              # 🎫 EventsModule (principal)
│   │       │   ├── partners/        # PartnersController
│   │       │   ├── customers/       # CustomersController
│   │       │   ├── events/          # EventsController, Sections, Spots
│   │       │   └── orders/          # OrdersController
│   │       ├── rabbitmq/            # 🐰 RabbitmqModule
│   │       └── app.module.ts        # 🏠 Módulo raiz
│   │
│   └── emails/                       # 📧 Aplicação de Emails
│       └── src/
│           ├── consumer.service.ts   # Consome eventos do RabbitMQ
│           ├── emails.controller.ts
│           ├── emails.service.ts
│           └── emails.module.ts
│
├── docker-compose.yml                # 🐳 MySQL, Redis, RabbitMQ
└── package.json
```

### Camadas da Arquitetura (DDD)

Este projeto segue **Domain-Driven Design (DDD)** com camadas bem definidas:

```
┌─────────────────────────────────────────┐
│  Apresentação (Controllers)             │  ← HTTP/REST
├─────────────────────────────────────────┤
│  Aplicação (Services)                   │  ← Casos de uso
├─────────────────────────────────────────┤
│  Domínio (Entidades, Value Objects)     │  ← Regras de negócio
├─────────────────────────────────────────┤
│  Infraestrutura (Repositories, DB)      │  ← Persistência
└─────────────────────────────────────────┘
```

**1. Camada de Domínio** (`@core/events/domain/`)
- **Responsabilidade:** Contém as regras de negócio puras
- **Componentes:**
  - **Entidades:** Partner, Event, Customer, Order, SpotReservation
  - **Value Objects:** EventSectionId, EventSpotId, CustomerId, etc.
  - **Eventos de Domínio:** PartnerCreated, OrderCreated, etc.
  - **Interfaces de Repositório:** Define contratos sem implementação

**2. Camada de Aplicação** (`@core/events/application/`)
- **Responsabilidade:** Coordena casos de uso, orquestra operações
- **Componentes:**
  - **Services:** PartnerService, EventService, OrderService
  - **Handlers:** Processam eventos de domínio
  - **DTOs:** Objetos de transferência de dados

**3. Camada de Infraestrutura** (`@core/events/infra/`)
- **Responsabilidade:** Implementações técnicas (banco, filas, etc.)
- **Componentes:**
  - **Repositories:** Implementações MySQL dos repositórios
  - **Schemas:** Mapeamento ORM (MikroORM)

**4. Camada de Apresentação** (`events/`, `controllers/`)
- **Responsabilidade:** Expõe a API REST
- **Componentes:**
  - **Controllers:** Recebem requisições HTTP

---

## 📦 Módulos Detalhados

### 1. AppModule (Módulo Raiz)
**Arquivo:** `apps/mba-ddd-venda-ingresso/src/app.module.ts`

**Função:** Módulo principal que **importa e conecta todos os outros módulos**.

```typescript
@Module({
  imports: [
    DatabaseModule,      // Conexão com MySQL
    BullModule.forRoot({ // Configuração do Redis/Bull para filas
      redis: {
        host: 'localhost',
        port: 6379,
      },
    }),
    EventsModule,        // Módulo principal de negócio
    DomainEventsModule,  // Sistema de eventos de domínio
    ApplicationModule,   // Serviço de aplicação (transações)
    RabbitmqModule,      // Mensageria RabbitMQ
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
```

**Por que existe?**
- Ponto de entrada da aplicação
- Conecta todos os módulos
- Configura infraestrutura global (Redis, RabbitMQ)

---

### 2. DatabaseModule
**Arquivo:** `apps/mba-ddd-venda-ingresso/src/database/database.module.ts`

**Função:** Configura a **conexão com o banco de dados MySQL** usando MikroORM.

```typescript
@Global()  // ← Disponível em toda aplicação sem precisar importar
@Module({
  imports: [
    MikroOrmModule.forRoot({
      entities: [  // 📋 Lista todas as tabelas do banco
        CustomerSchema,
        PartnerSchema,
        EventSchema,
        EventSectionSchema,
        EventSpotSchema,
        OrderSchema,
        SpotReservationSchema,
        StoredEventSchema,
      ],
      dbName: 'events',
      host: 'localhost',
      port: 3306,
      user: 'root',
      password: 'root',
      type: 'mysql',
    }),
  ],
  providers: [
    {
      provide: 'IUnitOfWork',  // ← Nome usado para injeção
      useFactory(em: EntityManager) {
        return new UnitOfWorkMikroOrm(em);
      },
      inject: [EntityManager],
    },
  ],
  exports: ['IUnitOfWork'],
})
export class DatabaseModule {}
```

**Componentes Principais:**

#### MikroORM
- ORM (Object-Relational Mapping) para mapear objetos TypeScript para tabelas MySQL
- Gerencia conexões, transações e queries

#### UnitOfWork (IUnitOfWork)
- **Padrão de Projeto** que agrupa operações de banco em uma transação
- **Função:** Garantir que todas as operações sejam salvas juntas ou nenhuma seja salva
- **Exemplo de uso:**
```typescript
// Tudo dentro de runTransaction é salvo junto
await uow.runTransaction(async () => {
  await orderRepo.add(order);
  await spotReservationRepo.add(reservation);
  await eventRepo.add(event);
  // Se qualquer operação falhar, NADA é salvo (rollback)
});
```

**Por que é importante?**
- Sem este módulo, a aplicação não consegue salvar ou buscar dados
- Centraliza configuração do banco

---

### 3. EventsModule (Módulo Principal de Negócio)
**Arquivo:** `apps/mba-ddd-venda-ingresso/src/events/events.module.ts`

**Função:** Módulo **MAIS IMPORTANTE** da aplicação. Gerencia toda a lógica de negócio.

#### O que ele gerencia?
1. **Parceiros** (Partner) - Organizadores de eventos
2. **Clientes** (Customer) - Compradores de ingressos
3. **Eventos** (Event) - Shows, festas, jogos
4. **Seções** (EventSection) - Áreas do evento (VIP, Pista, Camarote)
5. **Spots** (EventSpot) - Lugares individuais (Cadeira A1, B2)
6. **Pedidos** (Order) - Compras de ingressos
7. **Reservas** (SpotReservation) - Reserva temporária de lugares

#### Estrutura do Módulo

```typescript
@Module({
  imports: [
    // 1️⃣ Registra os schemas (tabelas) que este módulo usa
    MikroOrmModule.forFeature([
      CustomerSchema,
      PartnerSchema,
      EventSchema,
      EventSectionSchema,
      EventSpotSchema,
      OrderSchema,
      SpotReservationSchema,
    ]),

    // 2️⃣ Importa ApplicationModule para usar ApplicationService
    ApplicationModule,

    // 3️⃣ Cria fila para eventos de integração
    BullModule.registerQueue({
      name: 'integration-events',
    }),
  ],

  // 4️⃣ PROVIDERS: Repositórios e Services
  providers: [
    // Repositórios (acesso ao banco)
    {
      provide: 'IPartnerRepository',
      useFactory: (em: EntityManager) => new PartnerMysqlRepository(em),
      inject: [EntityManager],
    },
    {
      provide: 'ICustomerRepository',
      useFactory: (em: EntityManager) => new CustomerMysqlRepository(em),
      inject: [EntityManager],
    },
    {
      provide: 'IEventRepository',
      useFactory: (em: EntityManager) => new EventMysqlRepository(em),
      inject: [EntityManager],
    },
    {
      provide: 'IOrderRepository',
      useFactory: (em: EntityManager) => new OrderMysqlRepository(em),
      inject: [EntityManager],
    },
    {
      provide: 'ISpotReservationRepository',
      useFactory: (em: EntityManager) => new SpotReservationMysqlRepository(em),
      inject: [EntityManager],
    },

    // Services (lógica de negócio)
    {
      provide: PartnerService,
      useFactory: (partnerRepo, appService) =>
        new PartnerService(partnerRepo, appService),
      inject: ['IPartnerRepository', ApplicationService],
    },
    {
      provide: CustomerService,
      useFactory: (customerRepo, uow) =>
        new CustomerService(customerRepo, uow),
      inject: ['ICustomerRepository', 'IUnitOfWork'],
    },
    {
      provide: EventService,
      useFactory: (eventRepo, partnerRepo, uow) =>
        new EventService(eventRepo, partnerRepo, uow),
      inject: ['IEventRepository', 'IPartnerRepository', 'IUnitOfWork'],
    },
    {
      provide: OrderService,
      useFactory: (orderRepo, customerRepo, eventRepo, spotReservationRepo, uow, paymentGateway) =>
        new OrderService(orderRepo, customerRepo, eventRepo, spotReservationRepo, uow, paymentGateway),
      inject: ['IOrderRepository', 'ICustomerRepository', 'IEventRepository', 'ISpotReservationRepository', 'IUnitOfWork', PaymentGateway],
    },
    PaymentGateway,  // Simula gateway de pagamento

    // Handler de eventos
    {
      provide: MyHandlerHandler,
      useFactory: (partnerRepo, domainEventManager) =>
        new MyHandlerHandler(partnerRepo, domainEventManager),
      inject: ['IPartnerRepository', DomainEventManager],
    },
  ],

  // 5️⃣ CONTROLLERS: Recebem requisições HTTP
  controllers: [
    PartnersController,      // /partners
    CustomersController,     // /customers
    EventsController,        // /events
    EventSectionsController, // /events/:id/sections
    EventSpotsController,    // /events/:id/spots
    OrdersController,        // /orders
  ],
})
export class EventsModule implements OnModuleInit {
  constructor(
    private readonly domainEventManager: DomainEventManager,
    private moduleRef: ModuleRef,
    @InjectQueue('integration-events')
    private integrationEventsQueue: Queue<IIntegrationEvent>,
  ) {}

  // 6️⃣ Executa quando o módulo inicia
  onModuleInit() {
    // Registra handlers para eventos de domínio
    MyHandlerHandler.listensTo().forEach((eventName: string) => {
      this.domainEventManager.register(eventName, async (event) => {
        const handler = await this.moduleRef.resolve(MyHandlerHandler);
        await handler.handle(event);
      });
    });

    // Quando um parceiro é criado, publica evento de integração
    this.domainEventManager.registerForIntegrationEvent(
      PartnerCreated.name,
      async (event) => {
        const integrationEvent = new PartnerCreatedIntegrationEvent(event);
        await this.integrationEventsQueue.add(integrationEvent);
      },
    );
  }
}
```

#### Controllers do EventsModule

**PartnersController** (`/partners`)
```typescript
@Controller('partners')
export class PartnersController {
  constructor(private partnerService: PartnerService) {}

  @Get()
  list() {
    return this.partnerService.list();  // Lista todos os parceiros
  }

  @Post()
  create(@Body() body: { name: string }) {
    return this.partnerService.create(body);  // Cria novo parceiro
  }
}
```

**EventsController** (`/events`)
```typescript
@Controller('events')
export class EventsController {
  constructor(private eventService: EventService) {}

  @Get()
  list() {
    return this.eventService.findEvents();  // Lista todos os eventos
  }

  @Post()
  create(@Body() body: EventDto) {
    return this.eventService.create(body);  // Cria novo evento
  }

  @Put(':event_id/publish-all')
  publish(@Param('event_id') event_id: string) {
    return this.eventService.publishAll({ event_id });  // Publica evento
  }
}
```

**OrdersController** (`/orders`)
```typescript
@Controller('orders')
export class OrdersController {
  constructor(private orderService: OrderService) {}

  @Get()
  list() {
    return this.orderService.list();  // Lista todos os pedidos
  }

  @Post()
  create(@Body() body: CreateOrderDto) {
    return this.orderService.create(body);  // Cria pedido (compra ingresso)
  }
}
```

---

### 4. DomainEventsModule
**Arquivo:** `apps/mba-ddd-venda-ingresso/src/domain-events/domain-events.module.ts`

**Função:** Gerencia o **sistema de eventos de domínio**. Quando algo importante acontece (ex: parceiro criado, pedido realizado), este módulo notifica outros componentes.

```typescript
@Global()
@Module({
  imports: [
    MikroOrmModule.forFeature([StoredEventSchema])  // Tabela para armazenar eventos
  ],
  providers: [
    DomainEventManager,           // Gerenciador de eventos
    IntegrationEventsPublisher,   // Publica eventos no RabbitMQ
    {
      provide: 'IStoredEventRepository',
      useFactory: (em) => new StoredEventMysqlRepository(em),
      inject: [EntityManager],
    },
  ],
  exports: [DomainEventManager],
})
export class DomainEventsModule implements OnModuleInit {
  constructor(
    private readonly domainEventManager: DomainEventManager,
    private moduleRef: ModuleRef,
  ) {}

  onModuleInit() {
    // Salva TODOS os eventos no banco (para auditoria)
    this.domainEventManager.register('*', async (event: IDomainEvent) => {
      const repo = await this.moduleRef.resolve('IStoredEventRepository');
      await repo.add(event);
    });
  }
}
```

#### Componentes Principais

**DomainEventManager**
- Sistema de pub/sub (publicação/assinatura) para eventos
- Permite que componentes "escutem" eventos e reajam a eles
```typescript
// Exemplo de uso
domainEventManager.register('PartnerCreated', async (event) => {
  console.log('Um novo parceiro foi criado!', event);
  // Enviar email de boas-vindas
});
```

**IntegrationEventsPublisher**
- Processa eventos da fila Redis e publica no RabbitMQ
```typescript
@Processor('integration-events')
export class IntegrationEventsPublisher {
  constructor(private amqpConnection: AmqpConnection) {}

  @Process()
  async handle(job: Job<IIntegrationEvent>) {
    // Publica evento no RabbitMQ
    await this.amqpConnection.publish(
      'amq.direct',        // Exchange
      job.data.event_name, // RoutingKey
      job.data,            // Dados do evento
    );
  }
}
```

**StoredEventRepository**
- Salva todos os eventos em uma tabela no banco
- Útil para auditoria, debug e event sourcing

**Por que é importante?**
- Desacopla componentes (não precisam se conhecer diretamente)
- Permite rastrear tudo que acontece no sistema
- Facilita comunicação entre aplicações (via RabbitMQ)

---

### 5. ApplicationModule
**Arquivo:** `apps/mba-ddd-venda-ingresso/src/application/application.module.ts`

**Função:** Fornece o **ApplicationService**, responsável por gerenciar transações e eventos.

```typescript
@Module({
  providers: [
    {
      provide: ApplicationService,
      useFactory: (uow: IUnitOfWork, domainEventManager: DomainEventManager) => {
        return new ApplicationService(uow, domainEventManager);
      },
      inject: ['IUnitOfWork', DomainEventManager],
    },
  ],
  exports: [ApplicationService],
})
export class ApplicationModule {}
```

#### ApplicationService

**Responsabilidades:**
1. Iniciar e finalizar transações
2. Publicar eventos de domínio após transações bem-sucedidas
3. Garantir consistência (tudo salvo ou nada salvo)

```typescript
export class ApplicationService {
  constructor(
    private uow: IUnitOfWork,
    private domainEventManager: DomainEventManager
  ) {}

  async run<T>(callback: () => Promise<T>): Promise<T> {
    await this.start();  // Inicia transação
    try {
      const result = await callback();  // Executa operação
      await this.finish();              // Finaliza e publica eventos
      return result;
    } catch (e) {
      await this.fail();  // Rollback em caso de erro
      throw e;
    }
  }

  async finish() {
    const aggregateRoots = this.uow.getAggregateRoots();

    // 1. Publica eventos de domínio
    for (const aggregateRoot of aggregateRoots) {
      await this.domainEventManager.publish(aggregateRoot);
    }

    // 2. Salva no banco
    await this.uow.commit();

    // 3. Publica eventos de integração
    for (const aggregateRoot of aggregateRoots) {
      await this.domainEventManager.publishForIntegrationEvent(aggregateRoot);
    }
  }
}
```

**Exemplo de uso:**
```typescript
export class PartnerService {
  constructor(
    private partnerRepo: IPartnerRepository,
    private appService: ApplicationService
  ) {}

  async create(input: { name: string }) {
    // ApplicationService garante transação e publicação de eventos
    return await this.appService.run(async () => {
      const partner = Partner.create(input);
      await this.partnerRepo.add(partner);
      return partner;
    });
  }
}
```

**Por que é importante?**
- Garante que banco e eventos estejam sincronizados
- Evita inconsistências (dados salvos sem eventos ou vice-versa)

---

### 6. RabbitmqModule
**Arquivo:** `apps/mba-ddd-venda-ingresso/src/rabbitmq/rabbitmq.module.ts`

**Função:** Configura **conexão com RabbitMQ** para comunicação entre aplicações.

```typescript
@Global()
@Module({
  imports: [
    RabbitMQModule.forRoot(RabbitMQModule, {
      uri: 'amqp://admin:admin@localhost:5672',
      connectionInitOptions: { wait: false },
    }),
    RabbitmqModule,
  ],
  exports: [RabbitMQModule],
})
export class RabbitmqModule {}
```

#### O que é RabbitMQ?

**RabbitMQ** é um **message broker** (correio de mensagens). Permite que diferentes aplicações se comuniquem sem se conhecerem diretamente.

**Analogia:** Grupo do WhatsApp
- Você posta uma mensagem no grupo
- Todos os inscritos recebem
- Você não precisa enviar para cada pessoa individualmente

#### Conceitos do RabbitMQ

```
Produtor → Exchange → Queue → Consumidor
```

- **Exchange**: Centro de distribuição de mensagens (como uma agência de correios)
- **Queue**: Fila onde mensagens ficam armazenadas até serem consumidas
- **RoutingKey**: "Endereço" da mensagem (define para qual fila vai)
- **Producer**: Quem envia a mensagem (nossa API principal)
- **Consumer**: Quem recebe e processa (serviço de emails)

**Por que é importante?**
- Permite comunicação assíncrona entre aplicações
- Garante que mensagens não se percam (são armazenadas na fila)
- Permite escalar aplicações independentemente

---

### 7. EmailsModule (Aplicação Separada)
**Arquivo:** `apps/emails/src/emails.module.ts`

**Função:** Aplicação **consumidora de eventos**. Escuta RabbitMQ e processa eventos.

```typescript
@Module({
  imports: [
    RabbitMQModule.forRoot(RabbitMQModule, {
      uri: 'amqp://admin:admin@localhost:5672',
      connectionInitOptions: { wait: false },
    }),
  ],
  controllers: [EmailsController],
  providers: [EmailsService, ConsumerService],
})
export class EmailsModule {}
```

#### ConsumerService

```typescript
@Injectable()
export class ConsumerService {
  @RabbitSubscribe({
    exchange: 'amq.direct',                        // De qual exchange escutar
    routingKey: 'PartnerCreatedIntegrationEvent',  // Quais eventos processar
    queue: 'emails',                               // Nome da fila
  })
  handle(msg: { event_name: string; [key: string]: any }) {
    console.log('ConsumerService.handle', msg);
    // Aqui você enviaria um email de verdade
    // Ex: this.emailService.sendWelcomeEmail(msg.partner_name, msg.partner_email);
  }
}
```

**Como funciona:**
1. API principal cria um parceiro
2. Evento `PartnerCreated` é disparado
3. Evento vai para fila Redis → RabbitMQ
4. **ConsumerService** recebe o evento
5. Processa (envia email, notificação, etc.)

**Por que é uma aplicação separada?**
- Desacoplamento: API não precisa esperar email ser enviado
- Escalabilidade: Pode ter várias instâncias processando emails
- Resiliência: Se serviço de email cair, API continua funcionando

---

## 🔄 Fluxos de Negócio

### Fluxo 1: Criando um Parceiro

**Objetivo:** Criar um novo parceiro (organizador de eventos)

#### Passo a Passo

**1. Cliente HTTP faz requisição**
```http
POST http://localhost:3000/partners
Content-Type: application/json

{
  "name": "Live Nation Brasil"
}
```

**2. PartnersController recebe** (`partners.controller.ts:14`)
```typescript
@Post()
create(@Body() body: { name: string }) {
  return this.partnerService.create(body);  // Delega para o service
}
```

**3. PartnerService processa** (`partner.service.ts:15-21`)
```typescript
async create(input: { name: string }) {
  return await this.applicationService.run(async () => {
    const partner = Partner.create(input);  // Cria entidade de domínio
    await this.partnerRepo.add(partner);    // Salva no banco
    return partner;
  });
}
```

**4. Entidade Partner dispara evento**
Quando `Partner.create()` é chamado, a entidade dispara:
```typescript
export class Partner extends AggregateRoot {
  static create(command: { name: string }): Partner {
    const partnerId = PartnerId.create();
    const partner = new Partner({
      partner_id: partnerId,
      name: command.name,
    });

    // 🔥 Dispara evento de domínio
    partner.dispatchEvent(
      new PartnerCreated(partnerId.value, command.name)
    );

    return partner;
  }
}
```

**5. ApplicationService finaliza transação** (`application.service.ts:13-22`)
```typescript
async finish() {
  const aggregateRoots = this.uow.getAggregateRoots();

  // A) Publica eventos de domínio (internos)
  for (const aggregateRoot of aggregateRoots) {
    await this.domainEventManager.publish(aggregateRoot);
  }

  // B) Commit no banco (salva de verdade)
  await this.uow.commit();

  // C) Publica eventos de integração (externos)
  for (const aggregateRoot of aggregateRoots) {
    await this.domainEventManager.publishForIntegrationEvent(aggregateRoot);
  }
}
```

**6. Evento de Integração vai para fila Redis** (`events.module.ts:166-173`)
```typescript
this.domainEventManager.registerForIntegrationEvent(
  PartnerCreated.name,
  async (event) => {
    const integrationEvent = new PartnerCreatedIntegrationEvent(event);
    await this.integrationEventsQueue.add(integrationEvent);  // 📤 Adiciona na fila
  }
);
```

**7. IntegrationEventsPublisher processa fila** (`integration-events.publisher.ts:10-20`)
```typescript
@Process()
async handle(job: Job<IIntegrationEvent>) {
  await this.amqpConnection.publish(
    'amq.direct',        // Exchange do RabbitMQ
    job.data.event_name, // RoutingKey: "PartnerCreatedIntegrationEvent"
    job.data,            // Payload completo
  );
}
```

**8. Serviço de Emails consome evento** (`consumer.service.ts:6-19`)
```typescript
@RabbitSubscribe({
  exchange: 'amq.direct',
  routingKey: 'PartnerCreatedIntegrationEvent',
  queue: 'emails',
})
handle(msg: { event_name: string; partner_id: string; name: string }) {
  console.log('Novo parceiro criado:', msg);
  // Aqui enviaria email: this.emailService.sendWelcomeEmail(msg);
}
```

#### Diagrama do Fluxo Completo

```
┌─────────────────┐
│  Cliente HTTP   │
│  POST /partners │
└────────┬────────┘
         ↓
┌────────────────────┐
│ PartnersController │ (Recebe requisição)
└────────┬───────────┘
         ↓
┌────────────────┐
│ PartnerService │ (Lógica de negócio)
└────────┬───────┘
         ↓
┌──────────────────────┐
│ Partner.create()     │ (Cria entidade + dispara PartnerCreated)
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ PartnerRepository    │ (Salva no MySQL)
└──────────┬───────────┘
           ↓
┌──────────────────────────┐
│ ApplicationService       │ (Commit + publica eventos)
└──────────┬───────────────┘
           ↓
┌──────────────────────────┐
│ DomainEventManager       │ (Publica eventos internos)
└──────────┬───────────────┘
           ↓
┌──────────────────────────┐
│ Fila Redis (Bull)        │ (integration-events)
└──────────┬───────────────┘
           ↓
┌──────────────────────────┐
│ IntegrationEventsPublisher│ (Processa fila)
└──────────┬───────────────┘
           ↓
┌──────────────────────────┐
│ RabbitMQ (amq.direct)    │ (Message Broker)
└──────────┬───────────────┘
           ↓
┌──────────────────────────┐
│ ConsumerService          │ (Serviço de Emails)
│ (Envia email)            │
└──────────────────────────┘
```

---

### Fluxo 2: Criando um Pedido (Comprando Ingresso)

**Objetivo:** Cliente compra um ingresso para um evento

#### Passo a Passo

**1. Cliente HTTP faz requisição**
```http
POST http://localhost:3000/orders
Content-Type: application/json

{
  "event_id": "uuid-do-evento",
  "section_id": "uuid-da-secao",
  "spot_id": "uuid-do-lugar",
  "customer_id": "uuid-do-cliente",
  "card_token": "token-cartao-credito"
}
```

**2. OrdersController recebe** (`orders.controller.ts`)
```typescript
@Post()
create(@Body() body: CreateOrderDto) {
  return this.orderService.create(body);
}
```

**3. OrderService processa** (`order.service.ts:26-103`)

```typescript
async create(input: CreateOrderDto) {
  // A) Busca cliente
  const customer = await this.customerRepo.findById(input.customer_id);
  if (!customer) throw new Error('Customer not found');

  // B) Busca evento
  const event = await this.eventRepo.findById(input.event_id);
  if (!event) throw new Error('Event not found');

  // C) Verifica se spot está disponível
  const sectionId = new EventSectionId(input.section_id);
  const spotId = new EventSpotId(input.spot_id);

  if (!event.allowReserveSpot({ section_id: sectionId, spot_id: spotId })) {
    throw new Error('Spot not available');
  }

  // D) Verifica se já não está reservado
  const spotReservation = await this.spotReservationRepo.findById(spotId);
  if (spotReservation) {
    throw new Error('Spot already reserved');
  }

  // E) Executa em transação
  return this.uow.runTransaction(async () => {
    // 1. Cria reserva temporária
    const spotReservationCreated = SpotReservation.create({
      spot_id: spotId,
      customer_id: customer.id,
    });
    await this.spotReservationRepo.add(spotReservationCreated);
    await this.uow.commit();  // Salva reserva

    try {
      // 2. Processa pagamento
      const section = event.sections.find((s) => s.id.equals(sectionId));
      await this.paymentGateway.payment({
        token: input.card_token,
        amount: section.price,
      });

      // 3. Cria pedido com status "pago"
      const order = Order.create({
        customer_id: customer.id,
        event_spot_id: spotId,
        amount: section.price,
      });
      order.pay();  // Marca como pago
      await this.orderRepo.add(order);

      // 4. Marca spot como reservado no evento
      event.markSpotAsReserved({
        section_id: sectionId,
        spot_id: spotId,
      });
      this.eventRepo.add(event);

      await this.uow.commit();  // Salva tudo
      return order;

    } catch (e) {
      // ❌ Pagamento falhou: cria pedido cancelado
      const order = Order.create({
        customer_id: customer.id,
        event_spot_id: spotId,
        amount: section.price,
      });
      order.cancel();
      this.orderRepo.add(order);
      await this.uow.commit();
      throw new Error('Aconteceu um erro ao reservar o seu lugar');
    }
  });
}
```

#### Diagrama do Fluxo

```
┌────────────────┐
│ Cliente HTTP   │
│ POST /orders   │
└───────┬────────┘
        ↓
┌───────────────────┐
│ OrdersController  │
└───────┬───────────┘
        ↓
┌───────────────────────────────────────────────────┐
│ OrderService.create()                             │
├───────────────────────────────────────────────────┤
│ 1. Busca Customer                                 │
│ 2. Busca Event                                    │
│ 3. Valida se spot está disponível                 │
│ 4. Verifica se já não está reservado              │
└───────┬───────────────────────────────────────────┘
        ↓
┌───────────────────────────────────────────────────┐
│ UnitOfWork.runTransaction()                       │
├───────────────────────────────────────────────────┤
│ 5. Cria SpotReservation (reserva temporária)      │
│ 6. Commit (salva reserva)                         │
│                                                   │
│ 7. PaymentGateway.payment() (processa pagamento)  │
│    ✅ Sucesso                    ❌ Falha          │
│       ↓                             ↓             │
│   Cria Order (pago)          Cria Order (cancelado)│
│   Marca spot como reservado   Commit              │
│   Commit                                          │
└───────────────────────────────────────────────────┘
```

**Eventos disparados:**
- `SpotReservationCreated` (quando reserva é criada)
- `OrderCreated` (quando pedido é criado)
- `OrderPaid` ou `OrderCancelled` (dependendo do pagamento)

---

### Fluxo 3: Criando e Publicando um Evento

**Objetivo:** Parceiro cria um evento (show) e publica para venda

#### Etapa 1: Criar Evento

```http
POST http://localhost:3000/events
Content-Type: application/json

{
  "name": "Rock in Rio 2024",
  "description": "Maior festival de música do Brasil",
  "date": "2024-09-15T20:00:00Z",
  "partner_id": "uuid-do-parceiro"
}
```

**Processamento:**
```typescript
// EventsController → EventService
async create(input: CreateEventDto) {
  // 1. Busca parceiro
  const partner = await this.partnerRepo.findById(input.partner_id);
  if (!partner) throw new Error('Partner not found');

  // 2. Parceiro inicia o evento (agregado cria agregado)
  const event = partner.initEvent({
    name: input.name,
    date: input.date,
    description: input.description,
  });

  // 3. Salva evento
  this.eventRepo.add(event);
  await this.uow.commit();

  return event;
}
```

#### Etapa 2: Adicionar Seção ao Evento

```http
POST http://localhost:3000/events/{event_id}/sections
Content-Type: application/json

{
  "name": "Pista Premium",
  "description": "Área próxima ao palco",
  "total_spots": 500,
  "price": 250.00
}
```

**Processamento:**
```typescript
async addSection(input: AddSectionDto) {
  const event = await this.eventRepo.findById(input.event_id);
  if (!event) throw new Error('Event not found');

  // Entidade Event adiciona seção
  event.addSection({
    name: input.name,
    description: input.description,
    total_spots: input.total_spots,
    price: input.price,
  });

  await this.eventRepo.add(event);
  await this.uow.commit();

  return event;
}
```

#### Etapa 3: Publicar Evento (Liberar para Venda)

```http
PUT http://localhost:3000/events/{event_id}/publish-all
```

**Processamento:**
```typescript
async publishAll(input: { event_id: string }) {
  const event = await this.eventRepo.findById(input.event_id);
  if (!event) throw new Error('Event not found');

  // Publica todas as seções
  event.publishAll();

  await this.eventRepo.add(event);
  await this.uow.commit();

  return event;
}
```

**O que `event.publishAll()` faz:**
- Marca todas as seções como "published"
- Torna spots disponíveis para compra
- Dispara evento `EventPublished`

---

## 🎨 Padrões de Projeto Utilizados

### 1. Domain-Driven Design (DDD)

**O que é?** Abordagem que coloca o **domínio** (regras de negócio) no centro da aplicação.

**Conceitos DDD no projeto:**

#### Aggregate Root
- **O que é:** Entidade principal que garante consistência
- **Exemplos no projeto:**
  - `Partner` (raiz do agregado Partner)
  - `Event` (raiz do agregado Event, contém Sections e Spots)
  - `Order` (raiz do agregado Order)

```typescript
// Event é um Aggregate Root
export class Event extends AggregateRoot {
  private sections: EventSection[] = [];  // Agregados dentro do Event

  addSection(input: AddSectionInput) {
    // Event controla a criação de Sections
    const section = EventSection.create(input);
    this.sections.push(section);
    this.dispatchEvent(new EventAddedSection(this.event_id, section));
  }
}
```

#### Value Object
- **O que é:** Objeto sem identidade própria, definido apenas por seus valores
- **Exemplos no projeto:**
  - `EventSectionId`, `EventSpotId`, `CustomerId` (IDs tipados)
  - `Money` (se houvesse - representa valor monetário)

```typescript
export class EventSectionId {
  constructor(private readonly value: string) {}

  equals(other: EventSectionId): boolean {
    return this.value === other.value;
  }
}
```

#### Domain Events
- **O que é:** Eventos que representam algo que aconteceu no domínio
- **Exemplos no projeto:**
  - `PartnerCreated`, `EventCreated`, `OrderCreated`
  - `OrderPaid`, `OrderCancelled`
  - `EventPublished`

#### Repository Pattern
- **O que é:** Interface que abstrai acesso a dados
- **Benefício:** Domínio não sabe que existe banco de dados

```typescript
// Interface (domínio)
export interface IPartnerRepository {
  add(partner: Partner): Promise<void>;
  findById(id: string): Promise<Partner | null>;
  findAll(): Promise<Partner[]>;
}

// Implementação (infraestrutura)
export class PartnerMysqlRepository implements IPartnerRepository {
  // ... implementação com MikroORM
}
```

---

### 2. Unit of Work

**O que é?** Padrão que **agrupa operações de banco** em uma transação.

**Problema que resolve:**
```typescript
// ❌ Sem Unit of Work
await orderRepo.save(order);
await spotRepo.save(spot);
// Se spot.save() falhar, order já foi salvo (inconsistência!)

// ✅ Com Unit of Work
await uow.runTransaction(async () => {
  await orderRepo.save(order);
  await spotRepo.save(spot);
  // Tudo é salvo junto ou nada é salvo
});
```

**Implementação no projeto:**
```typescript
export interface IUnitOfWork {
  commit(): Promise<void>;                     // Salva tudo
  runTransaction<T>(fn: () => Promise<T>): Promise<T>;  // Executa em transação
  getAggregateRoots(): AggregateRoot[];        // Retorna entidades com eventos
}
```

---

### 3. Dependency Injection (Inversão de Controle)

**O que é?** Framework fornece as dependências automaticamente.

**Benefícios:**
- Facilita testes (pode injetar mocks)
- Reduz acoplamento
- Código mais limpo

**Exemplo:**
```typescript
// ❌ Sem DI (acoplado)
export class PartnerService {
  private partnerRepo = new PartnerMysqlRepository();  // Acoplado ao MySQL
}

// ✅ Com DI (desacoplado)
export class PartnerService {
  constructor(private partnerRepo: IPartnerRepository) {}  // Recebe interface
}
```

---

### 4. Event-Driven Architecture (EDA)

**O que é?** Arquitetura baseada em eventos. Componentes se comunicam através de eventos.

**Tipos de eventos no projeto:**

#### Domain Events (Eventos de Domínio)
- **Escopo:** Dentro da aplicação
- **Objetivo:** Comunicação entre agregados
- **Exemplos:** `PartnerCreated`, `OrderCreated`

#### Integration Events (Eventos de Integração)
- **Escopo:** Entre aplicações
- **Objetivo:** Comunicação via RabbitMQ
- **Exemplos:** `PartnerCreatedIntegrationEvent`

**Fluxo:**
```
Entidade dispara Domain Event
    ↓
DomainEventManager processa
    ↓
Converte para Integration Event
    ↓
Fila Redis (Bull)
    ↓
IntegrationEventsPublisher
    ↓
RabbitMQ
    ↓
Consumer (outras aplicações)
```

---

### 5. CQRS (Command Query Responsibility Segregation) - Parcial

**O que é?** Separa operações de leitura (queries) e escrita (commands).

**No projeto (simplificado):**
```typescript
export class EventService {
  // QUERY (leitura)
  findEvents() {
    return this.eventRepo.findAll();  // Só leitura
  }

  // COMMAND (escrita)
  async create(input: CreateEventDto) {
    // Valida, cria, salva, dispara eventos
  }
}
```

---

## 📋 Entidades de Domínio

### 1. Partner (Parceiro)
**Responsabilidade:** Organiza eventos

**Atributos:**
- `partner_id`: ID único
- `name`: Nome do parceiro

**Métodos:**
- `create(name)`: Cria novo parceiro
- `changeName(name)`: Altera nome
- `initEvent(data)`: Inicia um novo evento

**Eventos disparados:**
- `PartnerCreated`
- `PartnerNameChanged`

---

### 2. Event (Evento)
**Responsabilidade:** Representa um show, jogo, festa, etc.

**Atributos:**
- `event_id`: ID único
- `name`: Nome do evento
- `description`: Descrição
- `date`: Data e hora
- `is_published`: Se está publicado
- `partner_id`: ID do parceiro organizador
- `sections`: Lista de seções

**Métodos:**
- `create(data)`: Cria evento
- `addSection(data)`: Adiciona seção
- `changeSectionInformation(data)`: Atualiza seção
- `publishAll()`: Publica todas as seções
- `allowReserveSpot(data)`: Verifica se spot pode ser reservado
- `markSpotAsReserved(data)`: Marca spot como reservado

**Eventos disparados:**
- `EventCreated`
- `EventAddedSection`
- `EventPublished`
- `EventChangedName`, `EventChangedDescription`, `EventChangedDate`

---

### 3. EventSection (Seção de Evento)
**Responsabilidade:** Área do evento (VIP, Pista, Camarote)

**Atributos:**
- `section_id`: ID único
- `name`: Nome da seção
- `description`: Descrição
- `total_spots`: Total de lugares
- `price`: Preço
- `is_published`: Se está publicada
- `spots`: Lista de spots

**Métodos:**
- `create(data)`: Cria seção
- `publish()`: Publica seção (libera para venda)

---

### 4. EventSpot (Lugar/Cadeira)
**Responsabilidade:** Lugar individual em uma seção

**Atributos:**
- `spot_id`: ID único
- `location`: Localização (ex: "A1", "B15")
- `is_reserved`: Se está reservado
- `is_published`: Se está disponível para venda

**Métodos:**
- `create(location)`: Cria spot
- `publish()`: Publica spot
- `reserve()`: Reserva spot

---

### 5. Customer (Cliente)
**Responsabilidade:** Comprador de ingressos

**Atributos:**
- `customer_id`: ID único
- `name`: Nome
- `cpf`: CPF

**Métodos:**
- `create(data)`: Cria cliente
- `changeName(name)`: Altera nome

**Eventos disparados:**
- `CustomerCreated`
- `CustomerNameChanged`

---

### 6. Order (Pedido)
**Responsabilidade:** Compra de ingresso

**Atributos:**
- `order_id`: ID único
- `customer_id`: ID do cliente
- `amount`: Valor
- `event_spot_id`: ID do spot comprado
- `status`: Status (pending, paid, cancelled)

**Métodos:**
- `create(data)`: Cria pedido
- `pay()`: Marca como pago
- `cancel()`: Cancela pedido

**Eventos disparados:**
- `OrderCreated`
- `OrderPaid`
- `OrderCancelled`

---

### 7. SpotReservation (Reserva de Lugar)
**Responsabilidade:** Reserva temporária antes do pagamento

**Atributos:**
- `reservation_id`: ID único
- `spot_id`: ID do spot
- `customer_id`: ID do cliente

**Métodos:**
- `create(data)`: Cria reserva

**Eventos disparados:**
- `SpotReservationCreated`

---

## 🗄️ Schemas (Mapeamento do Banco)

O projeto usa **MikroORM** para mapear entidades TypeScript para tabelas MySQL.

**Exemplo de Schema:**
```typescript
export const PartnerSchema = new EntitySchema({
  class: Partner,
  tableName: 'partners',
  properties: {
    partner_id: {
      type: 'uuid',
      primary: true,
      onCreate: () => uuidv4(),
    },
    name: {
      type: 'string',
      length: 255,
    },
  },
});
```

**Tabelas no banco:**
- `partners`
- `customers`
- `events`
- `event_sections`
- `event_spots`
- `orders`
- `spot_reservations`
- `stored_events` (auditoria)

---

## 🚀 Como Executar

### Pré-requisitos
- Node.js 18+
- Docker e Docker Compose
- MySQL Client (opcional)

### Passo 1: Subir Infraestrutura
```bash
# Sobe MySQL, Redis e RabbitMQ
docker-compose up -d

# Verifica se estão rodando
docker ps
```

### Passo 2: Instalar Dependências
```bash
npm install
```

### Passo 3: Rodar Migrações
```bash
# Cria tabelas no banco
npx mikro-orm migration:up
```

### Passo 4: Rodar Aplicação Principal
```bash
# Terminal 1: API principal (porta 3000)
npm run start:dev
```

### Passo 5: Rodar Serviço de Emails
```bash
# Terminal 2: Serviço de emails (porta 3001)
npm run start:dev -- emails
```

### Passo 6: Testar API

**Criar parceiro:**
```bash
curl -X POST http://localhost:3000/partners \
  -H "Content-Type: application/json" \
  -d '{"name": "Live Nation"}'
```

**Criar cliente:**
```bash
curl -X POST http://localhost:3000/customers \
  -H "Content-Type: application/json" \
  -d '{"name": "João Silva", "cpf": "12345678900"}'
```

**Criar evento:**
```bash
curl -X POST http://localhost:3000/events \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rock in Rio 2024",
    "description": "Festival de música",
    "date": "2024-09-15T20:00:00Z",
    "partner_id": "<UUID_DO_PARCEIRO>"
  }'
```

**Adicionar seção:**
```bash
curl -X POST http://localhost:3000/events/<EVENT_ID>/sections \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pista Premium",
    "description": "Próximo ao palco",
    "total_spots": 100,
    "price": 250.00
  }'
```

**Publicar evento:**
```bash
curl -X PUT http://localhost:3000/events/<EVENT_ID>/publish-all
```

**Criar pedido:**
```bash
curl -X POST http://localhost:3000/orders \
  -H "Content-Type: application/json" \
  -d '{
    "event_id": "<EVENT_ID>",
    "section_id": "<SECTION_ID>",
    "spot_id": "<SPOT_ID>",
    "customer_id": "<CUSTOMER_ID>",
    "card_token": "tok_visa"
  }'
```

---

## 🔍 Acessando Interfaces de Gerenciamento

### RabbitMQ Management
```
URL: http://localhost:15672
Usuário: admin
Senha: admin
```

### MySQL
```bash
docker exec -it <container_mysql> mysql -u root -proot events
```

---

## 📊 Diagrama de Arquitetura Completo

```
┌──────────────────────────────────────────────────────────────┐
│                      APLICAÇÃO PRINCIPAL                      │
│                   (mba-ddd-venda-ingresso)                    │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐ │
│  │   Partners     │  │    Events      │  │    Orders      │ │
│  │  Controller    │  │  Controller    │  │  Controller    │ │
│  └───────┬────────┘  └───────┬────────┘  └───────┬────────┘ │
│          ↓                   ↓                    ↓          │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐ │
│  │   Partner      │  │     Event      │  │     Order      │ │
│  │    Service     │  │    Service     │  │    Service     │ │
│  └───────┬────────┘  └───────┬────────┘  └───────┬────────┘ │
│          ↓                   ↓                    ↓          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │            ApplicationService (Transações)           │   │
│  └──────────┬───────────────────────────────────────────┘   │
│             ↓                                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         DomainEventManager (Pub/Sub Eventos)         │   │
│  └──────────┬───────────────────────────────────────────┘   │
│             ↓                                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │        UnitOfWork (MikroORM + MySQL)                 │   │
│  └──────────┬───────────────────────────────────────────┘   │
│             ↓                                                │
└──────────────┼──────────────────────────────────────────────┘
               ↓
        ┌──────────────┐
        │    MySQL     │ (Banco de dados)
        └──────────────┘

               ↓
        ┌──────────────────────┐
        │ Fila Redis (Bull)    │
        │ integration-events   │
        └──────┬───────────────┘
               ↓
        ┌──────────────────────┐
        │IntegrationEvents     │
        │   Publisher          │
        └──────┬───────────────┘
               ↓
        ┌──────────────────────┐
        │    RabbitMQ          │
        │   (amq.direct)       │
        └──────┬───────────────┘
               ↓
┌──────────────────────────────────────────────┐
│        APLICAÇÃO DE EMAILS                   │
├──────────────────────────────────────────────┤
│  ┌──────────────────────────────┐            │
│  │   ConsumerService            │            │
│  │   @RabbitSubscribe           │            │
│  │   (Escuta eventos)           │            │
│  └──────────┬───────────────────┘            │
│             ↓                                │
│  ┌──────────────────────────────┐            │
│  │   EmailService               │            │
│  │   (Envia emails)             │            │
│  └──────────────────────────────┘            │
└──────────────────────────────────────────────┘
```

---

## 🎯 Resumo dos Módulos

| Módulo | Responsabilidade | Controllers | Services | Analogia |
|--------|------------------|-------------|----------|----------|
| **AppModule** | Módulo raiz, conecta todos os outros | AppController | AppService | Centro de comando da cidade |
| **DatabaseModule** | Conexão com MySQL via MikroORM | - | UnitOfWork | Ponte para o banco de dados |
| **EventsModule** | Lógica de negócio principal | Partners, Events, Orders, Customers | Partner, Event, Order, Customer | Coração da aplicação |
| **DomainEventsModule** | Sistema de eventos internos | - | DomainEventManager, IntegrationEventsPublisher | Central de notificações |
| **ApplicationModule** | Transações e coordenação | - | ApplicationService | Gerente de operações |
| **RabbitmqModule** | Comunicação entre aplicações | - | AmqpConnection | Correio entre serviços |
| **EmailsModule** | Processa eventos e envia emails | EmailsController | ConsumerService, EmailsService | Serviço de notificações |

---

## 📚 Conceitos de NestJS vs DDD

| Conceito NestJS | Equivalente DDD | Descrição |
|-----------------|-----------------|-----------|
| Module | Bounded Context | Limite de contexto de negócio |
| Controller | Interface Adapter | Adaptador para entrada HTTP |
| Service | Application Service | Orquestra casos de uso |
| Provider | Infrastructure | Implementação técnica |
| Entity (MikroORM) | Aggregate Root | Entidade raiz de domínio |

---

## 🧪 Testando o Sistema

### Cenário Completo: Do Parceiro ao Pedido

**1. Criar Parceiro**
```bash
POST /partners
{"name": "Rock Nation"}
# Resposta: {"partner_id": "uuid-1", "name": "Rock Nation"}
```

**2. Criar Cliente**
```bash
POST /customers
{"name": "Maria Silva", "cpf": "12345678900"}
# Resposta: {"customer_id": "uuid-2", ...}
```

**3. Criar Evento**
```bash
POST /events
{
  "name": "Festival 2024",
  "date": "2024-12-01T20:00:00Z",
  "partner_id": "uuid-1"
}
# Resposta: {"event_id": "uuid-3", ...}
```

**4. Adicionar Seção**
```bash
POST /events/uuid-3/sections
{
  "name": "Pista",
  "total_spots": 10,
  "price": 100
}
```

**5. Publicar Evento**
```bash
PUT /events/uuid-3/publish-all
# Agora spots estão disponíveis!
```

**6. Listar Spots**
```bash
GET /events/uuid-3/sections/uuid-4/spots
# Resposta: [{"spot_id": "uuid-5", "location": "A1", ...}, ...]
```

**7. Criar Pedido**
```bash
POST /orders
{
  "event_id": "uuid-3",
  "section_id": "uuid-4",
  "spot_id": "uuid-5",
  "customer_id": "uuid-2",
  "card_token": "tok_visa"
}
# Resposta: {"order_id": "uuid-6", "status": "paid", ...}
```

**8. Verificar Logs do Serviço de Emails**
```bash
# No terminal do serviço de emails, você verá:
# ConsumerService.handle { event_name: 'PartnerCreatedIntegrationEvent', partner_id: 'uuid-1', name: 'Rock Nation' }
```

---

## 💡 Conclusão

Este projeto demonstra uma **arquitetura completa e profissional** para sistemas de venda de ingressos usando:

✅ **NestJS** - Framework modular e escalável
✅ **DDD** - Domínio rico e expressivo
✅ **Event-Driven** - Comunicação assíncrona entre componentes
✅ **CQRS** - Separação de leitura e escrita
✅ **Microservices** - Aplicações independentes e desacopladas
✅ **Clean Architecture** - Camadas bem definidas
✅ **Unit of Work** - Transações consistentes
✅ **Repository Pattern** - Abstração de persistência
✅ **Message Broker** - Comunicação via RabbitMQ
✅ **Background Jobs** - Processamento assíncrono com Bull

**Principais aprendizados:**
1. NestJS organiza código em módulos reutilizáveis
2. DDD coloca regras de negócio no centro
3. Eventos permitem comunicação desacoplada
4. Transações garantem consistência de dados
5. Message brokers conectam aplicações de forma robusta

---

## 📖 Referências

- [NestJS Documentation](https://docs.nestjs.com/)
- [MikroORM Documentation](https://mikro-orm.io/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Domain-Driven Design (Eric Evans)](https://www.domainlanguage.com/ddd/)
- [Clean Architecture (Robert Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
