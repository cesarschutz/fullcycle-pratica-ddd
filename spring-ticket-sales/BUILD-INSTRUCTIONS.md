# Instruções de Build - Spring Ticket Sales

## Pré-requisitos

Antes de compilar o projeto, certifique-se de ter instalado:

### 1. Java Development Kit (JDK) 17 ou superior

**Verificar instalação:**
```bash
java -version
```

**Instalar (macOS com Homebrew):**
```bash
brew install openjdk@17
```

**Instalar (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### 2. Apache Maven 3.6 ou superior

**Verificar instalação:**
```bash
mvn -version
```

**Instalar (macOS com Homebrew):**
```bash
brew install maven
```

**Instalar (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install maven
```

### 3. MySQL (via Docker)

**Iniciar MySQL** (usando docker-compose do projeto principal):
```bash
# Na raiz do projeto fullcycle-pratica-ddd
docker-compose up -d mysql
```

Isso iniciará o MySQL na porta 3306 com:
- Usuário: `root`
- Senha: `root`
- Database: `events`

---

## Como Compilar

### Opção 1: Build completo (recomendado)

```bash
cd spring-ticket-sales
mvn clean install
```

Este comando:
- Limpa compilações anteriores
- Compila o código
- Executa testes (se houver)
- Gera o JAR executável em `target/`

### Opção 2: Apenas compilar (sem testes)

```bash
mvn clean compile
```

### Opção 3: Build sem testes

```bash
mvn clean install -DskipTests
```

---

## Como Executar

### Método 1: Com Maven (desenvolvimento)

```bash
mvn spring-boot:run
```

A aplicação iniciará em: `http://localhost:8080`

### Método 2: Executar JAR (produção)

Após o build completo:

```bash
java -jar target/spring-ticket-sales-1.0.0.jar
```

### Método 3: Com profile específico

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Verificando a Aplicação

### Health Check

```bash
curl http://localhost:8080/api/partners
```

Deve retornar uma lista vazia `[]` ou lista de parceiros.

### Logs

Os logs aparecerão no console mostrando:
- Inicialização do Spring Boot
- Conexão com MySQL
- Criação de tabelas (DDL)
- Requisições HTTP

---

## Troubleshooting

### Erro: "command not found: mvn"

**Solução**: Instale o Maven seguindo as instruções acima.

### Erro: "Cannot connect to MySQL"

**Solução**: Verifique se o MySQL está rodando:
```bash
docker ps | grep mysql
```

Se não estiver, inicie:
```bash
docker-compose up -d mysql
```

### Erro: "Port 8080 already in use"

**Solução 1**: Pare o processo usando a porta 8080.

**Solução 2**: Mude a porta no `application.properties`:
```properties
server.port=8081
```

### Erro: "Java version mismatch"

**Solução**: Certifique-se de estar usando Java 17+:
```bash
java -version
```

Se necessário, configure `JAVA_HOME`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

---

## Estrutura do Projeto Após Build

```
spring-ticket-sales/
├── target/                              # Gerado após build
│   ├── classes/                         # .class compilados
│   ├── spring-ticket-sales-1.0.0.jar   # JAR executável
│   └── maven-status/                    # Metadados do Maven
├── src/
├── pom.xml
└── ...
```

---

## Comandos Úteis Maven

### Limpar build anterior
```bash
mvn clean
```

### Compilar
```bash
mvn compile
```

### Executar testes
```bash
mvn test
```

### Gerar JAR
```bash
mvn package
```

### Instalar no repositório local
```bash
mvn install
```

### Ver árvore de dependências
```bash
mvn dependency:tree
```

### Atualizar dependências
```bash
mvn clean install -U
```

---

## IDEs Recomendadas

### IntelliJ IDEA
1. File → Open → Selecionar pasta `spring-ticket-sales`
2. Maven será detectado automaticamente
3. Run → Run 'TicketSalesApplication'

### Eclipse
1. File → Import → Maven → Existing Maven Projects
2. Selecionar pasta `spring-ticket-sales`
3. Run As → Spring Boot App

### VS Code
1. Instalar extensões:
   - Spring Boot Extension Pack
   - Java Extension Pack
2. Open Folder → `spring-ticket-sales`
3. F5 para debug

---

## Próximos Passos

Após build com sucesso:

1. ✅ Testar a API (ver README.md)
2. ✅ Ler documentação comparativa (COMPARACAO-NESTJS-SPRING.md)
3. ✅ Explorar o código
4. ✅ Adicionar mais funcionalidades

---

**Projeto criado para demonstrar Spring Boot vs NestJS em aplicações DDD**
