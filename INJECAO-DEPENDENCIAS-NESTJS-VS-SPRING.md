# Injeção de Dependências: NestJS vs Spring Boot

## 📋 Índice

1. [Introdução](#introdução)
2. [Como Funciona no NestJS](#como-funciona-no-nestjs)
3. [Exemplo Prático: CustomerService](#exemplo-prático-customerservice)
4. [Tipos de Providers no NestJS](#tipos-de-providers-no-nestjs)
5. [Como Funciona no Spring Boot](#como-funciona-no-spring-boot)
6. [Comparação Lado a Lado](#comparação-lado-a-lado)
7. [Vantagens e Desvantagens](#vantagens-e-desvantagens)

---

## 🎯 Introdução

**Injeção de Dependências (DI)** é um padrão de design onde, ao invés de uma classe criar suas próprias dependências, elas são **fornecidas (injetadas)** por um container externo.

### Por que usar DI?

**Sem DI (acoplamento forte)**:
```typescript
export class CustomerService {
  private customerRepo: ICustomerRepository;
  private uow: IUnitOfWork;

  constructor() {
    // ❌ Acoplamento: CustomerService conhece implementação concreta
    const em = new EntityManager(); // Como criar?
    this.customerRepo = new CustomerMysqlRepository(em);
    this.uow = new UnitOfWorkMikroOrm(em);
  }
}
```

**Problemas**:
- ❌ Difícil de testar (não pode usar mocks)
- ❌ Acoplado a implementações concretas
- ❌ Difícil de trocar implementações
- ❌ Responsabilidade extra: criar dependências

**Com DI (baixo acoplamento)**:
```typescript
export class CustomerService {
  constructor(
    private customerRepo: ICustomerRepository,  // ✅ Injetado!
    private uow: IUnitOfWork,                   // ✅ Injetado!
  ) {}
}
```

**Benefícios**:
- ✅ Fácil de testar (pode injetar mocks)
- ✅ Depende de interfaces, não implementações
- ✅ Fácil trocar implementações
- ✅ Responsabilidade única: lógica de negócio

---

## 🔧 Como Funciona no NestJS

### 1. Container de Injeção de Dependências

NestJS tem um **container IoC (Inversion of Control)** que:
1. **Registra** providers (classes, factories, valores)
2. **Resolve** dependências automaticamente
3. **Instancia** objetos na ordem correta
4. **Gerencia** ciclo de vida (singleton, transient, request-scoped)

### 2. Processo de Injeção

```
┌─────────────────────────────────────────────────────────┐
│  1. DECLARAÇÃO                                          │
│     Você declara o que precisa no construtor            │
│                                                          │
│     constructor(                                        │
│       private customerRepo: ICustomerRepository,        │
│       private uow: IUnitOfWork,                         │
│     ) {}                                                │
└───────────────────┬─────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  2. REGISTRO NO MÓDULO                                  │
│     Você diz ao NestJS COMO criar cada dependência      │
│                                                          │
│     providers: [                                        │
│       {                                                 │
│         provide: 'ICustomerRepository',  ← Token        │
│         useFactory: (em) => new CustomerMysqlRepository(em),│
│         inject: [EntityManager],                        │
│       },                                                │
│       {                                                 │
│         provide: 'IUnitOfWork',          ← Token        │
│         useFactory: (em) => new UnitOfWorkMikroOrm(em), │
│         inject: [EntityManager],                        │
│       }                                                 │
│     ]                                                   │
└───────────────────┬─────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  3. RESOLUÇÃO (pelo container NestJS)                   │
│                                                          │
│     a) Container vê que CustomerService precisa de:     │
│        - 'ICustomerRepository'                          │
│        - 'IUnitOfWork'                                  │
│                                                          │
│     b) Busca providers com esses tokens                 │
│                                                          │
│     c) Resolve dependências RECURSIVAMENTE:             │
│        - EntityManager (fornecido por MikroOrmModule)   │
│        - CustomerMysqlRepository(EntityManager)         │
│        - UnitOfWorkMikroOrm(EntityManager)              │
│                                                          │
│     d) Cria CustomerService com dependências injetadas  │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 Exemplo Prático: CustomerService

### Passo 1: A Classe CustomerService

**Arquivo**: `apps/mba-ddd-venda-ingresso/src/@core/events/application/customer.service.ts`

```typescript
export class CustomerService {
  constructor(
    private customerRepo: ICustomerRepository,  // Interface (não implementação!)
    private uow: IUnitOfWork,                   // Interface (não implementação!)
  ) {}

  async register(input: { name: string; cpf: string }) {
    const customer = Customer.create(input);
    this.customerRepo.add(customer);        // Usa a dependência injetada
    await this.uow.commit();                // Usa a dependência injetada
    return customer;
  }
}
```

**Pergunta**: Como o NestJS sabe qual implementação usar para `ICustomerRepository` e `IUnitOfWork`?

**Resposta**: Através do **registro no módulo**!

### Passo 2: Registro de ICustomerRepository

**Arquivo**: `apps/mba-ddd-venda-ingresso/src/events/events.module.ts` (linhas 63-67)

```typescript
@Module({
  providers: [
    {
      provide: 'ICustomerRepository',           // ← TOKEN (string)
      useFactory: (em: EntityManager) => new CustomerMysqlRepository(em),
      inject: [EntityManager],                  // ← Dependências da factory
    },
  ],
})
export class EventsModule {}
```

**Explicação linha por linha**:

1. **`provide: 'ICustomerRepository'`**
   - Define um **token** (identificador único)
   - É uma string, mas poderia ser um Symbol ou a própria classe
   - Este token será usado para INJETAR a dependência

2. **`useFactory: (em: EntityManager) => ...`**
   - Define uma **função factory** que cria a instância
   - Recebe `EntityManager` como parâmetro
   - Retorna `new CustomerMysqlRepository(em)`

3. **`inject: [EntityManager]`**
   - Lista as dependências que a factory precisa
   - NestJS vai INJETAR `EntityManager` quando chamar a factory
   - `EntityManager` vem do `MikroOrmModule`

### Passo 3: Registro de IUnitOfWork

**Arquivo**: `apps/mba-ddd-venda-ingresso/src/database/database.module.ts` (linhas 39-46)

```typescript
@Global()  // ← Torna disponível em TODOS os módulos
@Module({
  providers: [
    {
      provide: 'IUnitOfWork',                   // ← TOKEN
      useFactory(em: EntityManager) {
        return new UnitOfWorkMikroOrm(em);
      },
      inject: [EntityManager],                  // ← Dependência
    },
  ],
  exports: ['IUnitOfWork'],  // ← Exporta para outros módulos poderem usar
})
export class DatabaseModule {}
```

**Explicação**:

1. **`@Global()`**: Este módulo é global
   - Providers são disponibilizados para TODOS os módulos
   - Não precisa importar `DatabaseModule` em cada módulo

2. **`exports: ['IUnitOfWork']`**: Exporta o provider
   - Sem isso, o provider seria privado ao módulo
   - Com export, outros módulos podem usar

### Passo 4: Registro de CustomerService

**Arquivo**: `apps/mba-ddd-venda-ingresso/src/events/events.module.ts` (linhas 91-95)

```typescript
@Module({
  providers: [
    {
      provide: CustomerService,                  // ← TOKEN (classe)
      useFactory: (customerRepo, uow) =>
        new CustomerService(customerRepo, uow),
      inject: ['ICustomerRepository', 'IUnitOfWork'],  // ← Tokens das dependências
    },
  ],
})
export class EventsModule {}
```

**Explicação**:

1. **`provide: CustomerService`**: Token é a própria classe
   - Poderia ser string também: `'CustomerService'`
   - Classe é mais type-safe

2. **`inject: ['ICustomerRepository', 'IUnitOfWork']`**
   - Lista os TOKENS das dependências (strings!)
   - NestJS vai buscar providers com esses tokens
   - Ordem importa: primeira string → primeiro parâmetro da factory

### Passo 5: Fluxo Completo de Resolução

```
1. Controller pede CustomerService
   └─> NestJS vê: provide: CustomerService

2. CustomerService precisa de:
   ├─> 'ICustomerRepository'
   │   └─> NestJS busca: provide: 'ICustomerRepository'
   │       └─> Factory precisa de: EntityManager
   │           └─> NestJS busca: EntityManager (do MikroOrmModule)
   │               └─> Cria CustomerMysqlRepository(EntityManager)
   │
   └─> 'IUnitOfWork'
       └─> NestJS busca: provide: 'IUnitOfWork'
           └─> Factory precisa de: EntityManager
               └─> NestJS busca: EntityManager (já criado)
                   └─> Cria UnitOfWorkMikroOrm(EntityManager)

3. NestJS cria: new CustomerService(customerRepo, uow)

4. CustomerService pronto para usar!
```

---

## 🔀 Tipos de Providers no NestJS

### 1. Class Provider (useClass)

```typescript
providers: [
  {
    provide: PaymentGateway,
    useClass: PaymentGateway,
  },
  // Ou simplesmente:
  PaymentGateway,  // Shorthand
]
```

**Quando usar**: Quando a classe não tem dependências complexas.

### 2. Factory Provider (useFactory)

```typescript
providers: [
  {
    provide: 'ICustomerRepository',
    useFactory: (em: EntityManager) => {
      return new CustomerMysqlRepository(em);
    },
    inject: [EntityManager],
  },
]
```

**Quando usar**:
- ✅ Precisa de lógica customizada para criar instância
- ✅ Depende de outros providers
- ✅ Precisa de configuração dinâmica

**Exemplo deste projeto** (linhas 92-95):
```typescript
{
  provide: CustomerService,
  useFactory: (customerRepo, uow) => new CustomerService(customerRepo, uow),
  inject: ['ICustomerRepository', 'IUnitOfWork'],
}
```

### 3. Value Provider (useValue)

```typescript
providers: [
  {
    provide: 'DATABASE_CONFIG',
    useValue: {
      host: 'localhost',
      port: 3306,
    },
  },
]
```

**Quando usar**: Valores constantes, configurações, mocks.

### 4. Existing Provider (useExisting)

```typescript
providers: [
  CustomerService,
  {
    provide: 'CustomerServiceAlias',
    useExisting: CustomerService,  // Mesmo que CustomerService
  },
]
```

**Quando usar**: Criar alias para um provider existente.

---

## 🔄 Injeção por Token: String vs Class

### Opção 1: Token String (usado neste projeto)

```typescript
// Registro
providers: [
  {
    provide: 'ICustomerRepository',  // ← String token
    useFactory: (em) => new CustomerMysqlRepository(em),
    inject: [EntityManager],
  },
]

// Uso (precisa de @Inject decorator)
export class CustomerService {
  constructor(
    @Inject('ICustomerRepository') private customerRepo: ICustomerRepository,
    @Inject('IUnitOfWork') private uow: IUnitOfWork,
  ) {}
}
```

**Por que neste projeto não tem @Inject?**

Porque o `useFactory` do `CustomerService` recebe parâmetros **posicionais**:

```typescript
{
  provide: CustomerService,
  useFactory: (customerRepo, uow) => new CustomerService(customerRepo, uow),
  inject: ['ICustomerRepository', 'IUnitOfWork'],  // ← Ordem importa!
  //       ↑ posição 0              ↑ posição 1
  //       ↓                         ↓
  //    customerRepo                uow
}
```

### Opção 2: Token Class

```typescript
// Registro
providers: [
  CustomerMysqlRepository,  // provide e useClass implícitos
]

// Uso (NestJS infere pelo tipo)
export class CustomerService {
  constructor(
    private customerRepo: CustomerMysqlRepository,  // ← Sem @Inject!
  ) {}
}
```

**Vantagens de String Token**:
- ✅ Permite injetar interfaces (TypeScript)
- ✅ Desacoplamento: não depende de classe concreta
- ✅ Pode trocar implementação facilmente

**Desvantagens**:
- ⚠️ Precisa de `@Inject()` decorator (ou factory)
- ⚠️ Sem type-safety em tempo de compilação
- ⚠️ Strings podem ter typos

---

## 🌍 Escopo de Providers

### 1. Singleton (padrão)

```typescript
providers: [CustomerService]
```

- Uma **única instância** para toda aplicação
- Criada na inicialização
- Compartilhada entre todas as requisições

### 2. Request-scoped

```typescript
@Injectable({ scope: Scope.REQUEST })
export class CustomerService {}
```

- Nova instância para **cada requisição HTTP**
- Destruída após resposta

### 3. Transient

```typescript
@Injectable({ scope: Scope.TRANSIENT })
export class CustomerService {}
```

- Nova instância **toda vez** que é injetado
- Não compartilhado

---

## ☕ Como Funciona no Spring Boot

### 1. Registro Automático com @Component

**Spring** (Java):
```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // Spring cria implementação AUTOMATICAMENTE!
}

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    // Injeção por CONSTRUTOR (recomendado)
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}
```

**Como funciona**:

1. **Component Scanning**:
   - Spring escaneia pacotes procurando anotações
   - `@Service`, `@Repository`, `@Component`, `@Controller`
   - Registra automaticamente como beans

2. **Injeção Automática**:
   - Spring vê que `CustomerService` precisa de `CustomerRepository`
   - Busca bean do tipo `CustomerRepository`
   - Injeta automaticamente

**Nenhuma configuração manual!**

### 2. Comparação com NestJS

#### NestJS (manual):
```typescript
// 1. Criar implementação
export class CustomerMysqlRepository implements ICustomerRepository {
  // ... 50 linhas de código
}

// 2. Registrar no módulo
@Module({
  providers: [
    {
      provide: 'ICustomerRepository',
      useFactory: (em: EntityManager) => new CustomerMysqlRepository(em),
      inject: [EntityManager],
    },
    {
      provide: CustomerService,
      useFactory: (repo, uow) => new CustomerService(repo, uow),
      inject: ['ICustomerRepository', 'IUnitOfWork'],
    },
  ],
})
export class EventsModule {}
```

**Total**: ~15 linhas de configuração

#### Spring Boot (automático):
```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // Implementação gerada automaticamente pelo Spring Data JPA
}

@Service
public class CustomerService {
    public CustomerService(CustomerRepository repository) {
        // Injeção automática
    }
}
```

**Total**: 0 linhas de configuração! ✨

---

## 📊 Comparação Lado a Lado

### Cenário: Injetar Repository e UnitOfWork

#### NestJS

**1. Definir Interface**:
```typescript
export interface ICustomerRepository {
  add(customer: Customer): Promise<void>;
  findById(id: string): Promise<Customer | null>;
  findAll(): Promise<Customer[]>;
}
```

**2. Implementar**:
```typescript
export class CustomerMysqlRepository implements ICustomerRepository {
  constructor(private em: EntityManager) {}

  async add(customer: Customer): Promise<void> {
    await this.em.persistAndFlush(customer);
  }

  async findById(id: string): Promise<Customer | null> {
    return await this.em.findOne(Customer, { customer_id: id });
  }

  async findAll(): Promise<Customer[]> {
    return await this.em.find(Customer, {});
  }
}
```

**3. Registrar no Módulo**:
```typescript
@Module({
  providers: [
    // Registrar EntityManager (já vem do MikroOrmModule)

    // Registrar Repository
    {
      provide: 'ICustomerRepository',
      useFactory: (em: EntityManager) => new CustomerMysqlRepository(em),
      inject: [EntityManager],
    },

    // Registrar UnitOfWork
    {
      provide: 'IUnitOfWork',
      useFactory: (em: EntityManager) => new UnitOfWorkMikroOrm(em),
      inject: [EntityManager],
    },

    // Registrar Service
    {
      provide: CustomerService,
      useFactory: (repo, uow) => new CustomerService(repo, uow),
      inject: ['ICustomerRepository', 'IUnitOfWork'],
    },
  ],
})
export class EventsModule {}
```

**4. Usar**:
```typescript
export class CustomerService {
  constructor(
    private customerRepo: ICustomerRepository,
    private uow: IUnitOfWork,
  ) {}
}
```

**Linhas de código**:
- Interface: 4 linhas
- Implementação: ~50 linhas
- Configuração: ~20 linhas
- **Total: ~74 linhas**

#### Spring Boot

**1. Definir Repository** (interface):
```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // Pronto! Spring gera implementação automaticamente
}
```

**2. Usar**:
```java
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Transactional  // UnitOfWork automático!
    public Customer register(String name, String cpf) {
        Customer customer = Customer.create(name, cpf);
        return customerRepository.save(customer);
        // Commit automático ao final do método
    }
}
```

**Linhas de código**:
- Repository: 2 linhas
- Service: ~8 linhas
- Configuração: **0 linhas**
- **Total: ~10 linhas**

**Redução**: 87% menos código! 🚀

---

## 🔍 Detalhamento: Por que Spring é mais automático?

### 1. Component Scanning

**NestJS**:
```typescript
// Precisa registrar CADA provider manualmente
providers: [
  CustomerService,        // ← Manual
  PartnerService,         // ← Manual
  EventService,           // ← Manual
  OrderService,           // ← Manual
]
```

**Spring**:
```java
@SpringBootApplication  // ← Ativa component scan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Todas as classes com @Service, @Repository, @Component
// são AUTOMATICAMENTE registradas!
```

### 2. Injeção por Tipo vs Token

**NestJS**: Baseado em **tokens** (strings ou classes)
```typescript
inject: ['ICustomerRepository', 'IUnitOfWork']  // ← Strings
```

Problema: TypeScript não tem interfaces em runtime!
```typescript
// Não funciona! Interface não existe em JS
constructor(private repo: ICustomerRepository) {}

// Precisa de token:
constructor(@Inject('ICustomerRepository') private repo: ICustomerRepository) {}
```

**Spring**: Baseado em **tipos** (classes e interfaces)
```java
// Funciona! Java tem tipos em runtime
public CustomerService(CustomerRepository repository) {
    // Spring injeta pelo TIPO
}
```

### 3. Repository Pattern

**NestJS**: Implementação manual
```typescript
export class CustomerMysqlRepository implements ICustomerRepository {
  async add(customer: Customer): Promise<void> { ... }
  async findById(id: string): Promise<Customer | null> { ... }
  async findAll(): Promise<Customer[]> { ... }
  async delete(customer: Customer): Promise<void> { ... }
  // ... mais 10+ métodos
}
```

**Spring Data JPA**: Geração automática
```java
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // 20+ métodos gerados AUTOMATICAMENTE:
    // save, findById, findAll, delete, count, exists...
}
```

### 4. Unit of Work / Transaction Management

**NestJS**: Manual com UnitOfWork
```typescript
async register(input) {
  const customer = Customer.create(input);
  this.customerRepo.add(customer);
  await this.uow.commit();  // ← Manual!
  return customer;
}
```

**Spring**: Automático com @Transactional
```java
@Transactional  // ← Automático!
public Customer register(String name) {
    Customer customer = Customer.create(name);
    return repository.save(customer);
    // Commit automático ao final
}
```

---

## ⚖️ Vantagens e Desvantagens

### NestJS

#### Vantagens ✅
1. **Explícito e educacional**
   - Você vê exatamente o que está acontecendo
   - Ótimo para aprender DI e IoC

2. **Controle fino**
   - Pode customizar criação de cada instância
   - Factories permitem lógica complexa

3. **Flexível**
   - Pode injetar qualquer coisa (valores, funções, classes)
   - Tokens customizados (strings, symbols)

4. **TypeScript nativo**
   - Decorators elegantes
   - Type-safety (com cuidado)

#### Desvantagens ⚠️

1. **Muito código boilerplate**
   - Precisa registrar CADA provider
   - Factories verbosas para casos simples

2. **Configuração manual**
   - Fácil esquecer de registrar provider
   - Erros só aparecem em runtime

3. **Tokens são strings**
   - Sem type-safety real
   - Typos não são detectados em compile-time

4. **Ordem importa**
   - No array `inject`, ordem dos tokens deve corresponder aos parâmetros

### Spring Boot

#### Vantagens ✅

1. **Mínimo de configuração**
   - Component scan automático
   - Zero configuração para casos comuns

2. **Type-safe**
   - Injeção por tipo real (não strings)
   - Erros detectados em compile-time

3. **Convenção sobre configuração**
   - Faz a coisa certa automaticamente
   - Menos decisões a tomar

4. **Ecossistema maduro**
   - Spring Data JPA gera repositories
   - @Transactional gerencia transações
   - Inúmeras integrações prontas

#### Desvantagens ⚠️

1. **"Mágico"**
   - Pode ser difícil entender o que acontece por trás
   - Debugging pode ser complexo

2. **Menos controle fino**
   - Convenções podem não se adequar a casos específicos
   - Customização requer conhecimento avançado

3. **Curva de aprendizado**
   - Precisa entender proxies, AOP, reflection
   - Muitos conceitos do Spring

4. **Runtime overhead**
   - Reflection e proxies têm custo
   - Startup mais lento (inicialização do contexto)

---

## 🎯 Resumo Executivo

### NestJS: Configuração Manual

```typescript
// ❌ 20+ linhas de configuração manual
providers: [
  {
    provide: 'ICustomerRepository',
    useFactory: (em) => new CustomerMysqlRepository(em),
    inject: [EntityManager],
  },
  {
    provide: CustomerService,
    useFactory: (repo, uow) => new CustomerService(repo, uow),
    inject: ['ICustomerRepository', 'IUnitOfWork'],
  },
]
```

**Processo**:
1. Criar implementação manualmente
2. Registrar com factory
3. Listar dependências em `inject`
4. NestJS resolve baseado em tokens

### Spring Boot: Automático

```java
// ✅ 0 linhas de configuração!
@Service
public class CustomerService {
    public CustomerService(CustomerRepository repository) {
        // Spring injeta automaticamente
    }
}
```

**Processo**:
1. Anotar classe com `@Service`
2. Spring registra automaticamente (component scan)
3. Spring injeta automaticamente (por tipo)

---

## 🤔 Qual Escolher?

### Use NestJS quando:
- ✅ Quer entender profundamente como DI funciona
- ✅ Precisa de controle fino sobre instâncias
- ✅ Equipe domina TypeScript/Node.js
- ✅ Prioriza flexibilidade sobre produtividade

### Use Spring Boot quando:
- ✅ Quer máxima produtividade
- ✅ Precisa de ecossistema maduro e estável
- ✅ Equipe domina Java
- ✅ Prioriza convenção sobre configuração

---

## 📚 Conclusão

### NestJS
**Filosofia**: "Explicit is better than implicit"
- Você escreve TUDO
- Controle total, mas mais código
- Educacional e transparente

**Exemplo**: 74 linhas para configurar CustomerService completo

### Spring Boot
**Filosofia**: "Convention over configuration"
- Framework faz o trabalho pesado
- Menos código, mais produtividade
- "Mágico" mas muito eficiente

**Exemplo**: 10 linhas para CustomerService completo

### Redução de Código
**Spring Boot reduz ~85% do código de configuração de DI!**

Mas ambos implementam o mesmo padrão (Dependency Injection) e resolvem o mesmo problema: **desacoplamento e testabilidade**.

---

**Projeto**: NestJS DDD - Sistema de Venda de Ingressos
**Autor**: Documentação educacional
**Data**: Janeiro 2025
