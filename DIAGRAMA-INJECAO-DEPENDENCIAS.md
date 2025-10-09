# Diagramas de Injeção de Dependências

## 🎨 Visualização do Fluxo NestJS

### Exemplo Completo: CustomerService

```
┌─────────────────────────────────────────────────────────────────────┐
│                         INÍCIO DA APLICAÇÃO                         │
│                    (NestJS inicializa módulos)                      │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────┐
│  PASSO 1: REGISTRO DE PROVIDERS                                     │
│  ────────────────────────────────────────────────────────────────   │
│                                                                      │
│  DatabaseModule (Global):                                           │
│  ┌────────────────────────────────────────────┐                    │
│  │ provide: 'IUnitOfWork'                     │                    │
│  │ useFactory: (em) => new UnitOfWorkMikroOrm(em)                  │
│  │ inject: [EntityManager]                    │                    │
│  └────────────────────────────────────────────┘                    │
│                                                                      │
│  EventsModule:                                                      │
│  ┌────────────────────────────────────────────┐                    │
│  │ provide: 'ICustomerRepository'             │                    │
│  │ useFactory: (em) => new CustomerMysqlRepository(em)             │
│  │ inject: [EntityManager]                    │                    │
│  └────────────────────────────────────────────┘                    │
│                                                                      │
│  ┌────────────────────────────────────────────┐                    │
│  │ provide: CustomerService                   │                    │
│  │ useFactory: (repo, uow) =>                 │                    │
│  │    new CustomerService(repo, uow)          │                    │
│  │ inject: ['ICustomerRepository', 'IUnitOfWork']                  │
│  └────────────────────────────────────────────┘                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────┐
│  PASSO 2: CONTAINER DE DI INDEXA PROVIDERS                          │
│  ────────────────────────────────────────────────────────────────   │
│                                                                      │
│  Map de Providers:                                                  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Token                    │ Factory / Class                   │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │ EntityManager            │ (fornecido por MikroOrmModule)    │  │
│  │ 'IUnitOfWork'           │ (em) => UnitOfWorkMikroOrm(em)    │  │
│  │ 'ICustomerRepository'    │ (em) => CustomerMysqlRepository(em)│ │
│  │ CustomerService          │ (repo, uow) => CustomerService... │  │
│  └──────────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────┐
│  PASSO 3: REQUISIÇÃO HTTP CHEGA                                     │
│  ────────────────────────────────────────────────────────────────   │
│                                                                      │
│  POST /customers                                                    │
│  { "name": "João", "cpf": "12345678900" }                          │
│                                                                      │
│  ↓                                                                  │
│  CustomersController precisa de CustomerService                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────┐
│  PASSO 4: RESOLUÇÃO DE DEPENDÊNCIAS (Recursiva)                     │
│  ────────────────────────────────────────────────────────────────   │
│                                                                      │
│  Container precisa criar CustomerService:                           │
│                                                                      │
│  ┌────────────────────────────────────────┐                        │
│  │ CustomerService                        │                        │
│  │   ├─ precisa: 'ICustomerRepository'    │                        │
│  │   └─ precisa: 'IUnitOfWork'           │                        │
│  └────────────────────────────────────────┘                        │
│                    │                                                │
│                    ↓                                                │
│  Container busca 'ICustomerRepository':                             │
│                                                                      │
│  ┌────────────────────────────────────────┐                        │
│  │ 'ICustomerRepository'                  │                        │
│  │   useFactory: (em) => new CustomerMysqlRepository(em)          │
│  │   └─ precisa: EntityManager            │                        │
│  └────────────────────────────────────────┘                        │
│                    │                                                │
│                    ↓                                                │
│  Container busca EntityManager:                                     │
│  ✅ Já existe (singleton do MikroOrmModule)                        │
│                    │                                                │
│                    ↓                                                │
│  ✅ Cria CustomerMysqlRepository(EntityManager)                    │
│                                                                      │
│  ─────────────────────────────────────────────────────────────────  │
│                                                                      │
│  Container busca 'IUnitOfWork':                                     │
│                                                                      │
│  ┌────────────────────────────────────────┐                        │
│  │ 'IUnitOfWork'                         │                        │
│  │   useFactory: (em) => new UnitOfWorkMikroOrm(em)               │
│  │   └─ precisa: EntityManager            │                        │
│  └────────────────────────────────────────┘                        │
│                    │                                                │
│                    ↓                                                │
│  Container busca EntityManager:                                     │
│  ✅ Já existe (mesmo singleton)                                    │
│                    │                                                │
│                    ↓                                                │
│  ✅ Cria UnitOfWorkMikroOrm(EntityManager)                         │
│                                                                      │
│  ─────────────────────────────────────────────────────────────────  │
│                                                                      │
│  Agora tem todas as dependências:                                   │
│  ├─ customerRepo: CustomerMysqlRepository ✅                       │
│  └─ uow: UnitOfWorkMikroOrm ✅                                     │
│                    │                                                │
│                    ↓                                                │
│  ✅ Cria CustomerService(customerRepo, uow)                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────┐
│  PASSO 5: INJEÇÃO NO CONTROLLER                                     │
│  ────────────────────────────────────────────────────────────────   │
│                                                                      │
│  @Controller('customers')                                           │
│  export class CustomersController {                                 │
│    constructor(                                                     │
│      private customerService: CustomerService  ← ✅ INJETADO!      │
│    ) {}                                                             │
│                                                                      │
│    @Post()                                                          │
│    create(@Body() body) {                                           │
│      return this.customerService.register(body);                    │
│    }                                                                 │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Ciclo de Vida dos Providers

```
SINGLETON (padrão)
─────────────────────────────────────────────────────────────────
│
│  App Start                    Requisições                    App End
│     │                              │                            │
│     ↓                              ↓                            ↓
│  ┌──────┐                       ┌──────┐                    ┌──────┐
│  │CREATE│─────────────────────>│ REUSE│─────────────────────>│DESTROY│
│  └──────┘                       └──────┘                    └──────┘
│     ↑                              ↑
│     │                              │
│  Uma vez                      Mesma instância
│  no startup                   em todas requisições
│
─────────────────────────────────────────────────────────────────


REQUEST-SCOPED
─────────────────────────────────────────────────────────────────
│
│  Request 1         Request 2         Request 3
│     │                 │                 │
│     ↓                 ↓                 ↓
│  ┌──────┐         ┌──────┐         ┌──────┐
│  │CREATE│         │CREATE│         │CREATE│
│  └──┬───┘         └──┬───┘         └──┬───┘
│     │                │                │
│     ↓                ↓                ↓
│  ┌──────┐         ┌──────┐         ┌──────┐
│  │ USE  │         │ USE  │         │ USE  │
│  └──┬───┘         └──┬───┘         └──┬───┘
│     │                │                │
│     ↓                ↓                ↓
│  ┌──────┐         ┌──────┐         ┌──────┐
│  │DESTROY│         │DESTROY│         │DESTROY│
│  └──────┘         └──────┘         └──────┘
│
│  Nova instância para cada requisição
│
─────────────────────────────────────────────────────────────────


TRANSIENT
─────────────────────────────────────────────────────────────────
│
│  Injection 1      Injection 2      Injection 3
│     │                │                │
│     ↓                ↓                ↓
│  ┌──────┐         ┌──────┐         ┌──────┐
│  │CREATE│         │CREATE│         │CREATE│
│  └──┬───┘         └──┬───┘         └──┬───┘
│     │                │                │
│     ↓                ↓                ↓
│  Instância 1      Instância 2      Instância 3
│  (independente)   (independente)   (independente)
│
│  Nova instância toda vez que é injetado
│
─────────────────────────────────────────────────────────────────
```

---

## 🌳 Árvore de Dependências (CustomerService)

```
CustomersController
│
└─── CustomerService
     │
     ├─── ICustomerRepository ('ICustomerRepository')
     │    │
     │    └─── CustomerMysqlRepository
     │         │
     │         └─── EntityManager
     │              │
     │              └─── (fornecido por MikroOrmModule)
     │
     └─── IUnitOfWork ('IUnitOfWork')
          │
          └─── UnitOfWorkMikroOrm
               │
               └─── EntityManager (MESMO singleton acima)
```

### Observações Importantes:

1. **EntityManager é Singleton**
   - Criado UMA vez pelo MikroOrmModule
   - Compartilhado entre CustomerMysqlRepository e UnitOfWorkMikroOrm
   - Garante que ambos usam a mesma conexão/transação

2. **Ordem de Criação**:
   ```
   1º. EntityManager (MikroOrmModule)
   2º. CustomerMysqlRepository(EntityManager)
   3º. UnitOfWorkMikroOrm(EntityManager)
   4º. CustomerService(repository, uow)
   5º. CustomersController(customerService)
   ```

3. **Tokens String**:
   - `'ICustomerRepository'` → aponta para CustomerMysqlRepository
   - `'IUnitOfWork'` → aponta para UnitOfWorkMikroOrm
   - Permite trocar implementação sem mudar CustomerService

---

## 🆚 Comparação Visual: NestJS vs Spring

### NestJS: Configuração Explícita

```
┌─────────────────────────────────────────────────────────────────┐
│  VOCÊ ESCREVE:                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Interface ICustomerRepository                               │
│     ├─ add(customer): Promise<void>                             │
│     ├─ findById(id): Promise<Customer>                          │
│     └─ findAll(): Promise<Customer[]>                           │
│                                                                  │
│  2. Classe CustomerMysqlRepository                              │
│     └─ implements ICustomerRepository                           │
│        ├─ add() { ... 10 linhas }                               │
│        ├─ findById() { ... 10 linhas }                          │
│        └─ findAll() { ... 10 linhas }                           │
│                                                                  │
│  3. Registro no Módulo                                          │
│     providers: [                                                │
│       {                                                          │
│         provide: 'ICustomerRepository',                         │
│         useFactory: (em) => new CustomerMysqlRepository(em),    │
│         inject: [EntityManager],                                │
│       },                                                         │
│       {                                                          │
│         provide: CustomerService,                               │
│         useFactory: (repo, uow) =>                              │
│           new CustomerService(repo, uow),                       │
│         inject: ['ICustomerRepository', 'IUnitOfWork'],        │
│       },                                                         │
│     ]                                                            │
│                                                                  │
│  4. Classe CustomerService                                      │
│     constructor(                                                │
│       private repo: ICustomerRepository,                        │
│       private uow: IUnitOfWork,                                 │
│     ) {}                                                         │
│                                                                  │
│  TOTAL: ~80 linhas de código                                    │
└─────────────────────────────────────────────────────────────────┘
```

### Spring Boot: Configuração Automática

```
┌─────────────────────────────────────────────────────────────────┐
│  VOCÊ ESCREVE:                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Interface Repository (Spring Data JPA)                      │
│     @Repository                                                 │
│     public interface CustomerRepository                         │
│         extends JpaRepository<Customer, String> {               │
│       // Métodos gerados AUTOMATICAMENTE!                       │
│     }                                                            │
│                                                                  │
│  2. Classe CustomerService                                      │
│     @Service                                                    │
│     public class CustomerService {                              │
│       private final CustomerRepository repository;              │
│                                                                  │
│       public CustomerService(CustomerRepository repository) {   │
│         this.repository = repository; // INJEÇÃO AUTOMÁTICA     │
│       }                                                          │
│                                                                  │
│       @Transactional // Unit of Work AUTOMÁTICO                │
│       public Customer register(String name) {                   │
│         Customer c = Customer.create(name);                     │
│         return repository.save(c);                              │
│       }                                                          │
│     }                                                            │
│                                                                  │
│  3. Registro no Módulo                                          │
│     (NENHUM! Component scan automático)                         │
│                                                                  │
│  TOTAL: ~15 linhas de código                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  SPRING FAZ AUTOMATICAMENTE:                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ✅ Component Scan: encontra @Repository e @Service             │
│  ✅ Cria implementação do Repository (proxy dinâmico)          │
│  ✅ Injeta CustomerRepository em CustomerService               │
│  ✅ Gerencia transações com @Transactional                      │
│  ✅ EntityManager (Unit of Work) automático                     │
│  ✅ Commit/Rollback automático                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Resultado**: Spring reduz de 80 para 15 linhas = **81% menos código**!

---

## 📦 Módulos e Escopo

### NestJS: Módulos Explícitos

```
AppModule (raiz)
│
├─── DatabaseModule (@Global)
│    │
│    ├─── exports: ['IUnitOfWork']  ← Disponível para todos
│    └─── providers:
│         └─── 'IUnitOfWork': UnitOfWorkMikroOrm
│
├─── ApplicationModule
│    │
│    ├─── exports: [ApplicationService]
│    └─── providers:
│         └─── ApplicationService
│
└─── EventsModule
     │
     ├─── imports: [DatabaseModule, ApplicationModule]
     ├─── providers:
     │    ├─── 'ICustomerRepository': CustomerMysqlRepository
     │    ├─── 'IPartnerRepository': PartnerMysqlRepository
     │    ├─── CustomerService
     │    └─── PartnerService
     └─── controllers:
          ├─── CustomersController
          └─── PartnersController
```

**Regras**:
- Provider só é visível no próprio módulo
- Precisa `exports` para disponibilizar para outros
- Módulo importador precisa listar em `imports`
- `@Global()` torna disponível em todos (use com moderação)

### Spring Boot: Contexto Único

```
ApplicationContext (único)
│
├─── Component Scan (automático)
│    │
│    ├─── @Repository → CustomerRepository
│    ├─── @Service → CustomerService
│    ├─── @Service → PartnerService
│    └─── @Controller → CustomersController
│
└─── Beans (todos acessíveis)
     │
     ├─── CustomerRepository (singleton)
     ├─── CustomerService (singleton)
     ├─── PartnerService (singleton)
     └─── CustomersController (singleton)
```

**Regras**:
- Todos os beans são globais por padrão
- Component scan encontra automaticamente
- Não precisa de exports/imports
- Pode criar múltiplos contextos (avançado), mas raro

---

## 🎓 Resumo dos Conceitos

### Container de DI

**NestJS**:
- Container gerencia providers
- Baseado em tokens (strings ou classes)
- Resolução manual via providers array
- Módulos definem escopo

**Spring**:
- ApplicationContext gerencia beans
- Baseado em tipos (classes e interfaces)
- Resolução automática via component scan
- Contexto único (geralmente)

### Tokens de Injeção

**NestJS**:
```typescript
// Token pode ser:
provide: 'StringToken'           // String
provide: CustomerService          // Classe
provide: Symbol('CustomerRepo')   // Symbol

// Injeção:
@Inject('StringToken') private x  // Com decorator
```

**Spring**:
```java
// Sempre por tipo:
private CustomerRepository repo;  // Por tipo de classe

// Ou por nome (raro):
@Qualifier("customerRepo")
private CustomerRepository repo;
```

### Factories

**NestJS**:
```typescript
{
  provide: 'Token',
  useFactory: (dep1, dep2) => {
    // Lógica customizada
    return new MyClass(dep1, dep2);
  },
  inject: ['Dep1', 'Dep2'],
}
```

**Spring**:
```java
@Bean
public CustomerService customerService(CustomerRepository repo) {
    // Lógica customizada
    return new CustomerService(repo);
}
```

### Escopo

**NestJS**:
```typescript
@Injectable({ scope: Scope.DEFAULT })    // Singleton
@Injectable({ scope: Scope.REQUEST })    // Por requisição
@Injectable({ scope: Scope.TRANSIENT })  // Novo sempre
```

**Spring**:
```java
@Service                             // Singleton (padrão)
@Scope("request")                    // Por requisição
@Scope("prototype")                  // Novo sempre
```

---

## 🏆 Conclusão

### NestJS
- **Filosofia**: Explicit over Implicit
- **Vantagem**: Controle total, educacional
- **Desvantagem**: Muito código de configuração
- **Melhor para**: Aprender DI, controle fino

### Spring Boot
- **Filosofia**: Convention over Configuration
- **Vantagem**: Mínima configuração, produtividade
- **Desvantagem**: "Mágico", curva de aprendizado
- **Melhor para**: Produtividade, aplicações enterprise

**Ambos**: Implementam Dependency Injection com maestria! 🎯
