# ChurnInsight API 🎵

API de Machine Learning para predição de churn de usuários em plataformas de streaming de música, desenvolvida com Spring Boot e ONNX Runtime.

> 🏆 Projeto desenvolvido pela **Equipe DataBeats** para o **Hackathon ONE (Oracle Next Education)**

## 📋 Sobre o Projeto

ChurnInsight é uma aplicação que utiliza um modelo de Logistic Regression treinado com técnica SMOTE para prever a probabilidade de cancelamento (churn) de assinantes de serviços de música. A API recebe dados comportamentais do usuário e retorna a probabilidade de churn em tempo real.

### Características Principais

- ✅ **Inferência em tempo real** usando ONNX Runtime
- ✅ **Diagnóstico com IA** - Análise de fatores de risco e retenção
- ✅ **Processamento em Lote** - Até 1 milhão de registros (CSV/XLSX) com status em tempo real
- ✅ **Histórico de Predições** - Busca paginada com 15+ filtros avançados
- ✅ **Predições Pré-Calculadas** - Carregamento rápido de clientes do `clients.json`
- ✅ **Arquitetura Hexagonal** (Ports & Adapters) - Desacoplamento total
- ✅ **Cache em 2 camadas** - HTTP + Caffeine (memória)
- ✅ **Rate Limiting** por IP/usuário com bucket4j
- ✅ **Métricas** via Actuator/Prometheus (custom metrics)
- ✅ **Health Check** personalizado para modelo ONNX
- ✅ **Segurança** com Spring Security (HTTP Basic + CORS)
- ✅ **Containerização** com Docker & Docker Compose
- ✅ **Migrações automáticas** com Flyway
- ✅ **Processamento paralelo** - ThreadPool otimizado para 10K req/s

---

## 🏗️ Arquitetura

O projeto segue os princípios da **Arquitetura Hexagonal**:

```
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE ENTRADA                      │
│  ┌─────────────────┐          ┌──────────────────┐          │
│  │ REST Controller │          │  Rate Limiter    │          │
│  │   /predict      │  ──────► │   Filter         │          │
│  │   /stats        │          └──────────────────┘          │
│  └─────────────────┘                                        │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   CAMADA DE APLICAÇÃO                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │       ChurnPredictionService (Use Cases)             │   │
│  │  • PredictChurnUseCase                               │   │
│  │  • PredictionStatsUseCase                            │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE DOMÍNIO                      │
│  ┌──────────────────┐    ┌─────────────────────┐            │
│  │ CustomerProfile  │    │  ChurnStatus (Enum) │            │
│  │  (Value Object)  │    │  • WILL_CHURN       │            │
│  └──────────────────┘    │  • WILL_STAY        │            │
│                          └─────────────────────┘            │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   CAMADA DE INFRAESTRUTURA                  │
│  ┌───────────────────┐           ┌─────────────────────┐    │
│  │ OnnxRuntimeAdapter│           │ MySQLHistoryAdapter │    │
│  │  (Inferência ML)  │           │  (Persistência)     │    │
│  └───────────────────┘           └─────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚡ Novas Features (v2.0)

### 🎯 Diagnóstico de IA com Análise de Fatores

Cada predição agora retorna uma análise detalhada com:

```json
{
  "diagnosis": {
    "risk_factors": [
      "Alta taxa de skip (15%)",
      "Poucas anúncios escutados (0/semana)",
      "Idade jovem (correlação com churn)"
    ],
    "retention_factors": [
      "Tempo de escuta saudável (540min/dia)",
      "Assinatura Premium (maior lealdade)"
    ],
    "recommendation": "Ofereça recomendações personalizadas e reduza frequência de anúncios"
  }
}
```

### 📦 Processamento em Lote Ultra-Otimizado

- **Velocidade:** 10.000 registros/segundo (100K em 10s)
- **Suporte:** CSV e XLSX com até 1 milhão de registros
- **Tamanho:** Até 200MB por arquivo
- **Status em Tempo Real:** Acompanhe progresso do processamento
- **Persistência em Batch:** Multi-row INSERT com pool de threads
- **Recuperação:** Jobs persistidos em banco para retomar após reinício

**Exemplo de uso:**

```bash
curl -X POST http://localhost:10808/predict/batch \
  -u admin:Admin123 \
  -F "file=@clientes.csv"

# Retorna job_id para acompanhar progresso
# GET /predict/batch/status/{job_id}
```

### 🔍 Busca Avançada com 15+ Filtros

Histórico de predições com suporte a:

- Filtros por período (startDate, endDate)
- Filtros por probabilidade (minProbability, maxProbability)
- Filtros por status de churn (WILL_CHURN, WILL_STAY)
- Filtros demográficos (age, gender, country)
- Ordenação flexível (createdAt, probability, age, etc)
- Paginação (até 100 registros por página)

**Exemplo:**
```
GET /clients?page=0&size=50&sortBy=probability&churnStatus=WILL_CHURN&ageMin=20&ageMax=35&country=Brazil
```

### 📊 Predições Pré-Calculadas de Clientes

Carregamento rápido de predições pré-calculadas do `clients.json`:

- Cache em memória para O(1) lookup por clientId
- Índices para busca eficiente
- Estatísticas agregadas

### 📋 Contrato da API (Schema)

Endpoint `/api/contract` retorna esquema completo com:

- Validações esperadas
- Tipos e ranges de cada field
- Versão do modelo
- Compatibilidade de schema

### 🧬 Regras de Negócio Centralizadas

Classe `ChurnBusinessRules` com toda a lógica centralizada:

```java
public static boolean isChurning(double probability) {
    return probability > CHURN_THRESHOLD; // 0.412
}
```

---

## 🚀 Tecnologias

### Core
- **Java 21** (Eclipse Temurin)
- **Spring Boot 3.5.9**
- **Spring Security** (HTTP Basic Auth)
- **Spring Data JPA** + Hibernate
- **Spring Validation** (Jakarta Validation)

### Machine Learning
- **ONNX Runtime 1.19.2** (inferência do modelo)
- **Logistic Regression com SMOTE** (modelo treinado)

### Banco de Dados
- **MySQL 8.0** (mysql-connector-j)
- **Flyway** (migrações de schema)

### Cache & Performance
- **Caffeine Cache 3.1.8** (cache em memória)
- **Bucket4j 8.14.0** (rate limiting)

### Observabilidade
- **Spring Boot Actuator**
- **Micrometer** + **Prometheus Registry**
- **Custom Health Indicators**

### Processamento em Lote
- **Apache POI 5.2.4** (leitura de XLSX)
- **xlsx-streamer 2.1.0** (streaming para grandes arquivos XLSX)
- **Univocity Parsers 2.9.1** (parsing de CSV otimizado)

### Observabilidade
- **Spring Boot Actuator**
- **Micrometer** + **Prometheus Registry**
- **Custom Health Indicators** (modelo ONNX)
- **SpringDoc OpenAPI 2.8.15** (Swagger/OpenAPI 3.0)

### Utilitários
- **Lombok** (redução de boilerplate)
- **dotenv-java 3.2.0** (gerenciamento de variáveis de ambiente)
- **Log4j** (logging estruturado via SLF4J)

### Containerização
- **Docker** + **Docker Compose**
- **Multi-stage build** para otimização de imagem
- **ZGC Garbage Collector** para baixa latência

---

## 🧬 Internals - Como Funciona

### Pipeline de Batch Processing

O processamento em lote segue um pipeline otimizado:

```
Upload do Arquivo
    ↓
[Validação] - Verifica formato, tamanho e headers
    ↓
[Parsing Assíncrono] - Lê arquivo em chunks (2.500 registros)
    ↓
[Enriquecimento] - Mapeia CSV para CustomerProfile
    ↓
[Inferência Paralela] - 10 threads executam modelo ONNX (10K/s)
    ↓
[Persistência em Batch] - Multi-row INSERT com JDBC (1.500/chunk)
    ↓
[Job Status Update] - Atualiza progresso em real-time
    ↓
Processamento Completo
```

**Otimizações aplicadas:**

1. **Streaming:** Lê arquivo em chunks, não carrega tudo em memória
2. **Parsing Paralelo:** Univocity para CSV (mais rápido que Jackson)
3. **Inferência Paralela:** ThreadPool de 10 threads para ONNX
4. **Batch Inserts:** Multi-row INSERT com rewriteBatchedStatements
5. **JDBC direto:** Bypassa Hibernate ORM para inserts em massa
6. **Cache Manager:** Limpa cache automático após batch grande

### Cache em 2 Camadas

1. **HTTP Cache (Spring):** Decoradores `@Cacheable` em controllers
   - TTL: 30 minutos
   - Max size: 50.000 predições

2. **Caffeine Cache (Memória):** Cache local ultra-rápida
   - Lookup O(1) para predições repetidas
   - Eviction automática após TTL

### Arquitetura Hexagonal (Ports & Adapters)

```
┌─────────────────────────────────────┐
│      REST Controller (Input)        │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Use Case Interface (Application)  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Domain Model & Business Rules      │
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────┐
       ▼                ▼
┌──────────────┐  ┌──────────────────┐
│ Inference    │  │ Database         │
│ Port (ONNX)  │  │ Port (MySQL)     │
└──────────────┘  └──────────────────┘
```

### Rate Limiting com Bucket4j

Implementa algoritmo **Token Bucket**:

- 50 requisições/segundo por IP/usuário
- Burst capacity: 100 requisições
- Resposta 429 quando limite excedido
- Headers informativos (X-Rate-Limit-*)

### UUIDv7 para IDs

Cada predição recebe um ID único UUIDv7:

```java
String predictionId = UUIDv7.randomUUID().toString();
// Garante: sortable por timestamp + aleatório
// Melhor para índices em banco de dados
```

---

O modelo ONNX foi treinado com as seguintes características:

| Métrica | Valor |
|---------|-------|
| Accuracy | 51.44% |
| Precision | 26.76% |
| Recall | 50.48% |
| F1-Score | 34.98% |
| AUC-ROC | 50.05% |
| Threshold Ótimo | 0.412 |

**Features Numéricas:**
- `age`, `listening_time`, `songs_played_per_day`, `skip_rate`, `ads_listened_per_week`, `offline_listening`

**Features Categóricas:**
- `gender`, `country`, `subscription_type`, `device_type`

---

## ⚙️ Configuração e Execução

### Pré-requisitos

- Docker & Docker Compose instalados
- Porta `10808` (API) e `3306` (MySQL) disponíveis

### 1. Configurar Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
# Database
DB_NAME=churn_db
DB_ROOT_PASSWORD=seu_password_root_aqui
DB_USER=churn_user
DB_PASSWORD=seu_password_user_aqui
DB_URL=jdbc:mysql://localhost:3306/churn_db

# Security
SECURITY_USER=admin
SECURITY_PASSWORD=seu_password_seguro_aqui
SECURITY_ROLES=ADMIN
```

> ⚠️ **IMPORTANTE:** Nunca commite o arquivo `.env` no repositório! Ele já está no `.gitignore`.

### 2. Executar com Docker Compose

```bash
# Build e start dos containers
docker-compose up --build

# Ou em modo detached (background)
docker-compose up -d --build
```

A API estará disponível em: `http://localhost:10808`

### 3. Verificar Health Check

```bash
curl http://localhost:10808/actuator/health
```

Resposta esperada:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 983347249152,
        "free": 849019969536,
        "threshold": 10485760,
        "path": "/app/.",
        "exists": true
      }
    },
    "model": {
      "status": "UP",
      "details": {
        "status": "Modelo ONNX carregado com sucesso",
        "session": "Ativa"
      }
    },
    "ping": {
      "status": "UP"
    },
    "ssl": {
      "status": "UP",
      "details": {
        "validChains": [],
        "invalidChains": []
      }
    }
  }
}
```

---

## 📡 Endpoints da API

### 🔐 Autenticação

Todos os endpoints (exceto `/actuator/**`) requerem **HTTP Basic Authentication**.

Adicione o header:
```
Authorization: Basic base64(username:password)
```

### 1. Predição Simples com Diagnóstico

**POST** `/predict`

Realiza uma predição com diagnóstico completo de IA, incluindo fatores de risco e retenção.

**Request Body:**
```json
{
  "gender": "Male",
  "age": 29,
  "country": "Brazil",
  "subscriptionType": "Premium",
  "listeningTime": 540.0,
  "songsPlayedPerDay": 12,
  "skipRate": 0.15,
  "adsListenedPerWeek": 0,
  "deviceType": "Mobile",
  "offlineListening": true,
  "userId": "12345"
}
```

**Response:**
```json
{
  "label": "WILL_CHURN",
  "probability": 0.6087930798530579,
  "threshold": 0.412,
  "diagnosis": {
    "risk_factors": ["Alta taxa de skip", "Número baixo de anúncios"],
    "retention_factors": ["Tempo de escuta adequado"],
    "recommendation": "Ofereça recomendações personalizadas e reduza anúncios"
  },
  "confidence": 0.95,
  "latency_ms": 25
}
```

### 2. Predição com Estatísticas Completas

**POST** `/stats`

Retorna predição com probabilidades detalhadas de cada classe e análise do threshold.

**Request Body:** (mesmo formato do `/predict`)

**Response:**
```json
{
  "label": "WILL_CHURN",
  "probability": 0.6087930798530579,
  "probabilities": [0.6087931],
  "classProbabilities": {
    "WILL_CHURN": 0.6087931,
    "WILL_STAY": 0.39120692
  }
}
```

### 3. Processamento em Lote (CSV/XLSX)

**POST** `/predict/batch` (form-data)

Processa múltiplos perfis de clientes de forma assíncrona. Suporta até 1 milhão de registros.

**Request:**
- Form parameter: `file` (CSV ou XLSX)
- Tamanho máximo: 200MB

**Response (202 Accepted):**
```json
{
  "message": "Processamento iniciado com sucesso",
  "job_id": "batch-1234567890",
  "filename": "clientes.csv",
  "size_mb": 45.5,
  "estimated_time_minutes": 23,
  "status_url": "/predict/batch/status/batch-1234567890",
  "timestamp": 1704067200000
}
```

### 4. Métricas do Dashboard

**GET** `/dashboard/metrics`

**Response:**
```json
{
    "total_customers": 10000,
    "global_churn_rate": 97.5,
    "customers_at_risk": 9750,
    "revenue_at_risk": 150824.0,
    "model_accuracy": 0.5094
}
```

**Monitorar Progresso:**

**GET** `/predict/batch/status/{jobId}`

```json
{
  "job_id": "batch-1234567890",
  "status": "PROCESSING",
  "processed_count": 45000,
  "total_count": 100000,
  "progress_percentage": 45.0,
  "elapsed_time_seconds": 125,
  "estimated_remaining_seconds": 150,
  "error_count": 0,
  "churn_count": 25000,
  "stay_count": 20000
}
```

Possíveis status: `QUEUED`, `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`

### 5. Histórico de Predições

**GET** `/clients`

Busca paginada com filtros avançados de histórico de predições.

**Query Parameters:**
```
GET /clients?page=0&size=10&sortBy=createdAt&churnStatus=WILL_CHURN&startDate=2024-01-01&endDate=2024-12-31&minProbability=0.5
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid-predication-id",
      "userId": "12345",
      "gender": "Male",
      "age": 29,
      "country": "Brazil",
      "subscriptionType": "Premium",
      "churnStatus": "WILL_CHURN",
      "probability": 0.6087930798530579,
      "requestIp": "192.168.1.1",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1500,
  "totalPages": 150
}
```

**Filtros Disponíveis:**
- `page`: Página (0-indexed)
- `size`: Tamanho da página (max 100)
- `sortBy`: Campo de ordenação (createdAt, probability, age, etc)
- `churnStatus`: WILL_CHURN ou WILL_STAY
- `startDate`: Data inicial (yyyy-MM-dd)
- `endDate`: Data final (yyyy-MM-dd)
- `minProbability`: Probabilidade mínima
- `maxProbability`: Probabilidade máxima
- `ageMin` / `ageMax`: Range de idade
- `country`: Filtrar por país
- `gender`: Filtrar por gênero

### 6. Predições Pré-Calculadas de Clientes

**GET** `/clients/predictions`

Retorna lista de predições pré-calculadas carregadas do arquivo `clients.json`.

**Query Parameters:**
- `page`: Página (0-indexed, padrão: 0)
- `size`: Tamanho da página (max 100, padrão: 10)

**Response:**
```json
{
  "content": [
    {
      "clientId": "client-001",
      "churnStatus": "WILL_CHURN",
      "probability": 0.72,
      "features": { /* dados do cliente */ }
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50000
}
```

**GET** `/clients/predictions/search/{clientId}`

Busca predição específica de um cliente.

**GET** `/clients/predictions/stats`

Estatísticas agregadas das predições pré-calculadas.

```json
{
  "totalClients": 50000,
  "churnCount": 18500,
  "stayCount": 31500,
  "churnPercentage": 37.0,
  "avgProbability": 0.485,
  "maxProbability": 0.98,
  "minProbability": 0.05
}
```

### 7. Contrato da API

**GET** `/api/contract`

Retorna o contrato (schema) da API com detalhes de todas as features esperadas.

**Response:**
```json
{
  "version": "1.0",
  "modelVersion": "logistic-regression-v1",
  "features": {
    "numeric": [
      {"name": "age", "type": "integer", "min": 10, "max": 120},
      {"name": "listeningTime", "type": "double", "min": 0}
    ],
    "categorical": [
      {"name": "gender", "type": "string", "values": ["Male", "Female", "Other"]},
      {"name": "country", "type": "string"}
    ]
  }
}
```

### 8. Métricas e Observabilidade

**GET** `/actuator/metrics`

Retorna métricas detalhadas da aplicação.

**GET** `/actuator/prometheus`

Retorna métricas no formato Prometheus para integração com Prometheus/Grafana.

**GET** `/actuator/health`

Health check com componentes (Database, Model, Cache, etc)

---

## 🛡️ Segurança

### Rate Limiting

A API implementa rate limiting para evitar abuso:

- **50 requisições/segundo** por IP ou usuário autenticado
- **Burst capacity:** 100 requisições
- Resposta `429 Too Many Requests` quando o limite é excedido

Headers de resposta:
```
X-Rate-Limit-Limit: 100
X-Rate-Limit-Remaining: 87
```

### Validações

Todos os campos do `CustomerProfile` são validados:

| Campo | Validação |
|-------|-----------|
| `age` | Entre 10 e 120 |
| `listeningTime` | > 0 |
| `songsPlayedPerDay` | >= 0 |
| `skipRate` | Entre 0.0 e 1.0 |
| `adsListenedPerWeek` | >= 0 |
| Campos de texto | Não podem ser vazios |

---

## 📦 Estrutura do Projeto

```
src/main/java/com/hackathon/databeats/churninsight/
├── application/
│   ├── dto/                           # Data Transfer Objects
│   │   ├── PredictionResult.java      # Resultado de predição
│   │   ├── ClientPrediction.java      # Predição pré-calculada
│   │   ├── BatchProcessingStatus.java # Status do batch
│   │   └── PaginatedResponse.java     # Resposta paginada
│   ├── port/
│   │   ├── input/                     # Use Cases (interfaces)
│   │   │   ├── PredictChurnUseCase.java
│   │   │   ├── BatchProcessingUseCase.java
│   │   │   ├── PredictionStatsUseCase.java
│   │   │   ├── ClientPredictionQueryUseCase.java
│   │   │   └── ClientSearchUseCase.java
│   │   └── output/                    # Ports para adapters
│   │       ├── InferencePort.java
│   │       ├── SaveHistoryPort.java
│   │       ├── BatchSavePort.java
│   │       └── ModelMetadataPort.java
│   └── service/                       # Implementação dos Use Cases
│       ├── ApiContractQueryService.java
│       ├── ChurnPredictionService.java
│       ├── BatchProcessingService.java
│       ├── DashboardMetricsService.java
│       ├── PredictionHistoryService.java
│       └── ClientPredictionQueryService.java
├── domain/
│   ├── enums/
│   │   ├── ChurnStatus.java           # WILL_CHURN, WILL_STAY
│   │   └── BatchJobStatus.java
│   ├── exception/
│   │   ├── PredictionException.java
│   │   └── ModelInferenceException.java
│   ├── model/
│   │   ├── CustomerProfile.java       # Value Object
│   │   ├── PredictionHistory.java
│   │   └── BatchJob.java
│   └── rules/
│       ├── ChurnBusinessRules.java
│       └── ChurnDiagnosisService.java # Diagnóstico de IA
├── infra/
│   ├── adapter/
│   │   ├── input/
│   │   │   └── web/
│   │   │       ├── controller/
│   │   │       │   ├── PredictionController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   ├── ClientQueryController.java
│   │   │       │   └── ApiContractController.java
│   │   │       └── dto/
│   │   │           ├── CustomerProfileRequest.java
│   │   │           ├── PredictionStatsResponse.java
│   │   │           └── DashboardMetricsResponse.java
│   │   └── output/
│   │       ├── inference/
│   │       │   └── OnnxRuntimeAdapter.java
│   │       └── persistence/
│   │           ├── MySQLHistoryAdapter.java
│   │           ├── JdbcBatchPersistenceAdapter.java
│   │           └── repository/
│   │               ├── PredictionHistoryRepository.java
│   │               ├── PredictionHistoryEntity.java
│   │               └── PredictionHistorySpecification.java
│   ├── config/
│   │   ├── BeanConfiguration.java     # Configuração de beans
│   │   ├── SecurityConfig.java
│   │   ├── MetricsConfig.java
│   │   └── TaskExecutorConfig.java    # Executor para batch
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── ApiErrorResponse.java
│   ├── filter/
│   │   ├── RateLimitingFilter.java
│   │   └── CorsConfigurer.java
│   ├── health/
│   │   └── ModelHealthIndicator.java  # Health check customizado
│   └── util/
│       ├── ModelMetadata.java
│       ├── NetworkUtils.java
│       └── UUIDv7.java
└── ChurnInsightApplication.java

src/main/resources/
├── db/
│   └── migration/                     # Scripts Flyway
│       ├── V1__init_churn_history.sql
│       ├── V2__add_engineered_features.sql
│       └── V3__add_search_indexes.sql
├── clients.json                       # Predições pré-calculadas
├── metadata.json                      # Metadados do modelo
├── contrato_api.json                  # Contrato da API
├── modelo_hackathon.onnx              # Modelo ONNX
└── application.properties
```

---

## 🔧 Configurações Avançadas

### Cache (application.properties)

```properties
# Cache HTTP
app.cache.ttl-minutes=30
app.cache.max-size=50000

# Caffeine cache em memória para predições repetidas
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=50000,expireAfterWrite=30m
```

### Processamento em Lote (Batch)

```properties
# ThreadPool para processamento paralelo
app.batch.core-pool-size=6
app.batch.max-pool-size=10
app.batch.size=2500

# Threads dedicadas para inferência
app.batch.inference-threads=10

# Otimização de banco de dados
app.db.insert-threads=10
app.db.chunk-size=1500
```

### Rate Limiting

```properties
app.rate-limit.requests-per-second=50
app.rate-limit.burst-capacity=100
```

### File Upload

```properties
# Limite para arquivos de batch processing
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=200MB
```

### JVM Tuning (Dockerfile)

O container está configurado com:
- **ZGC (Z Garbage Collector)** para baixa latência e pause times < 1ms
- **MaxRAMPercentage=75%** (usa 75% da RAM do container)
- **Virtual Threads (Java 21+)** para melhor throughput

### CORS

```properties
# Origens permitidas
app.cors.allowed-origins=http://localhost:3000,http://127.0.0.1:3000,http://churn-frontend:3000
```

---

## 📈 Monitoramento

### Métricas Customizadas

- `houseprice.predictions.total` - Total de predições realizadas
- `houseprice.prediction.latency` - Latência de inferência (p50, p95, p99)
- `houseprice.requests.active` - Requisições ativas no momento
- `houseprice.errors.total` - Total de erros acumulados

### Health Check Customizado

O endpoint `/actuator/health` verifica múltiplos componentes:

✅ **Database (MySQL)** - Conectividade e validação do banco  
✅ **Disk Space** - Espaço disponível no container  
✅ **Model (ONNX)** - Modelo carregado e sessão ativa  
✅ **Ping** - Verificação básica de disponibilidade  
✅ **SSL** - Validação de certificados SSL/TLS

Todos os componentes devem estar com `status: "UP"` para a aplicação estar saudável.

---

## 🗄️ Schema do Banco de Dados

### Tabela Churn History (Histórico de Predições)

```sql
CREATE TABLE churn_history (
    id CHAR(36) PRIMARY KEY COMMENT 'UUIDv7 único por predição',
    
    -- Dados de entrada do cliente
    gender VARCHAR(20),
    age INT,
    country VARCHAR(10),
    subscription_type VARCHAR(30),
    listening_time DOUBLE,
    songs_played_per_day INT,
    skip_rate DOUBLE,
    ads_listened_per_week INT,
    device_type VARCHAR(30),
    offline_listening BOOLEAN,
    user_id CHAR(36),
    
    -- Saída do modelo
    churn_status ENUM('WILL_CHURN', 'WILL_STAY') NOT NULL,
    probability DOUBLE,
    
    -- Auditoria e rastreamento
    requester_id CHAR(36),
    request_ip VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices para performance
    KEY idx_churn_status (churn_status),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at),
    KEY idx_probability_desc (probability DESC),
    KEY idx_age_subscription (age, subscription_type),
    KEY idx_country_status (country, churn_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Migrações com Flyway

As migrações de banco de dados são gerenciadas automaticamente via **Flyway**:

**V1__init_churn_history.sql** - Criação da tabela base

**V2__add_engineered_features.sql** - Features derivadas

```sql
ALTER TABLE churn_history ADD COLUMN engagement_score FLOAT
  GENERATED ALWAYS AS (
    (listening_time / 1440.0) * (1 - skip_rate)
  ) STORED COMMENT 'Score de engajamento normalizado';
```

**V3__add_search_indexes.sql** - Índices de busca otimizados

```sql
CREATE INDEX idx_probability_desc ON churn_history(probability DESC);
CREATE INDEX idx_age_subscription ON churn_history(age, subscription_type);
CREATE INDEX idx_country_status ON churn_history(country, churn_status);
```

### Performance de Banco de Dados

Configurações aplicadas em `application.properties`:

```properties
# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=8
spring.datasource.hikari.connection-timeout=30000

# Batch Inserts (otimizado para batch processing)
spring.jpa.properties.hibernate.jdbc.batch_size=1500
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.jdbc.fetch_size=500

# JDBC rewrite para multi-row inserts (muito mais rápido)
spring.datasource.url=...&rewriteBatchedStatements=true&cachePrepStmts=true
```

**Resultado:** Inserts em batch 50-100x mais rápidos que individual

---

## 🧪 Testando a API

### Usando cURL

#### 1. Predição Simples

```bash
curl -X POST http://localhost:10808/predict \
  -u admin:Admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "gender": "Male",
    "age": 29,
    "country": "Brazil",
    "subscriptionType": "Premium",
    "listeningTime": 540.0,
    "songsPlayedPerDay": 12,
    "skipRate": 0.15,
    "adsListenedPerWeek": 0,
    "deviceType": "Mobile",
    "offlineListening": true,
    "userId": "12345"
  }'
```

#### 2. Processamento em Lote

```bash
# Iniciar processamento
curl -X POST http://localhost:10808/predict/batch \
  -u admin:Admin123 \
  -F "file=@clientes.csv"

# Exemplo de resposta:
# {
#   "message": "Processamento iniciado com sucesso",
#   "job_id": "batch-1704067200000",
#   "filename": "clientes.csv",
#   "size_mb": 45.5,
#   "estimated_time_minutes": 23,
#   "status_url": "/predict/batch/status/batch-1704067200000"
# }

# Acompanhar progresso (substituir JOB_ID)
curl -u admin:Admin123 \
  http://localhost:10808/predict/batch/status/batch-1704067200000
```

#### 3. Buscar Histórico com Filtros

```bash
# Buscar predições do últimos 7 dias com alta probabilidade de churn
curl -u admin:Admin123 \
  "http://localhost:10808/clients?page=0&size=20&sortBy=probability&churnStatus=WILL_CHURN&startDate=2024-01-01&minProbability=0.6"

# Buscar por idade específica
curl -u admin:Admin123 \
  "http://localhost:10808/clients?ageMin=20&ageMax=35&country=Brazil"
```

#### 4. Estatísticas de Predições Pré-Calculadas

```bash
curl -u admin:Admin123 \
  http://localhost:10808/clients/predictions/stats
```

#### 5. Consultar Contrato da API

```bash
curl http://localhost:10808/api/contract
```

#### 6. Métricas Prometheus

```bash
curl http://localhost:10808/actuator/prometheus
```

### Usando Postman/Insomnia

1. Configure **Authorization → Basic Auth**
2. Username: `admin`
3. Password: `Admin123` (ou valor em `.env`)
4. Body: JSON do CustomerProfile

**Import via Postman:**

Você pode importar a coleção OpenAPI diretamente de:
```
http://localhost:10808/v3/api-docs
```

### Criando Arquivo CSV para Batch

**Exemplo de `clientes.csv`:**

```csv
gender,age,country,subscriptionType,listeningTime,songsPlayedPerDay,skipRate,adsListenedPerWeek,deviceType,offlineListening,userId
Male,29,Brazil,Premium,540.0,12,0.15,0,Mobile,true,user-001
Female,35,USA,Free,320.5,8,0.25,5,Desktop,false,user-002
Male,22,Canada,Premium,680.2,15,0.08,0,Mobile,true,user-003
```

**Exemplo de `clientes.xlsx`:**

Crie uma planilha com as mesmas colunas e headers acima, em formato .xlsx

---

## 🐛 Troubleshooting

### Erro: "Modelo ONNX não carregado"

**Solução:** Verifique se o arquivo `modelo_hackathon.onnx` está em `src/main/resources/`

```bash
# Verificar se arquivo existe
ls -la src/main/resources/modelo_hackathon.onnx
```

### Erro: "Connection refused" ao MySQL

**Solução:** Aguarde o health check do MySQL estar pronto:
```bash
docker-compose logs db
# Aguarde até ver: "ready for connections"

# Ou verificar status:
docker-compose ps
```

### Erro 429 (Too Many Requests)

**Solução:** Você atingiu o rate limit. Aguarde alguns segundos ou aumente o limite em `application.properties`:

```properties
app.rate-limit.requests-per-second=100  # aumentar de 50 para 100
app.rate-limit.burst-capacity=200        # aumentar de 100 para 200
```

### Container da API não inicia

**Solução:** Verifique as variáveis de ambiente e logs:

```bash
docker-compose logs app
```

Erros comuns:
- `.env` não configurado
- MySQL não iniciou ainda
- Porta 10808 em uso

### Batch Processing lento

**Solução:** Aumentar threads pool em `application.properties`:

```properties
app.batch.max-pool-size=20          # aumentar de 10
app.batch.inference-threads=20       # aumentar de 10
app.db.insert-threads=20             # aumentar de 10
```

Ou aumentar RAM do container (dockerfile):
```dockerfile
ENV JAVA_OPTS="-Xmx2g -XX:+UseZGC"
```

### Erro 400 ao fazer batch: "File size exceeds maximum"

**Solução:** O arquivo é maior que 200MB. Dividir em partes menores:

```bash
# Dividir CSV em arquivos de 100MB
split -b 100M clientes.csv clientes_part_

# Processar cada parte
for file in clientes_part_*; do
  curl -X POST http://localhost:10808/predict/batch \
    -u admin:Admin123 \
    -F "file=@$file"
done
```

### Histórico vazio após predições

**Solução:** Verificar se Flyway rodou as migrations:

```bash
# Ver logs de migration
docker-compose logs app | grep "Flyway"

# Se necessário, resetar e aplicar
docker-compose down -v  # Remove volumes
docker-compose up --build
```

### Cache não funcionando

**Solução:** Verificar se Spring Cache está habilitado:

```properties
spring.cache.type=caffeine  # Deve estar presente
```

Limpar cache:
```bash
curl -X POST http://localhost:10808/cache/clear \
  -u admin:Admin123
```

### Métricas não aparecem em Prometheus

**Solução:** Verificar se endpoint está disponível:

```bash
curl http://localhost:10808/actuator/prometheus

# Se vazio, verificar se micrometer está habilitado
docker-compose logs app | grep "micrometer"
```

---

## 👥 Equipe DataBeats

### Time Back-End 💻
- [**Ezandro Bueno**](https://github.com/ezbueno)
- [**Jorge Filipi Dias**](https://github.com/jorgefilipi)
- [**Wanderson Souza**](https://github.com/wandersondevops)
- [**Wendell Dorta**](https://github.com/WendellD3v)

### Time Data Science 📊
- [**André Ribeiro**](https://github.com/andrerochads)
- [**Kelly Muehlmann**](https://github.com/kellymuehlmann)
- [**Luiz Alves**](https://github.com/lf-all)
- [**Mariana Fernandes**](https://github.com/mari-martins-fernandes)

---

## 📝 Licença

Este projeto foi desenvolvido para o **Hackathon ONE (Oracle Next Education)** pela **Equipe DataBeats**.

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

---

## 📧 Contato

Para dúvidas ou sugestões, abra uma issue no [repositório oficial](https://github.com/ezbueno/churninsight-api) ou entre em contato com a equipe.

---

**Desenvolvido com ❤️ pela Equipe DataBeats | Hackathon ONE (Oracle Next Education)**
