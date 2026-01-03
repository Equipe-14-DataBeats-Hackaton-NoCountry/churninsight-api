# ChurnInsight API 🎵

API de Machine Learning para predição de churn de usuários em plataformas de streaming de música, desenvolvida com Spring Boot e ONNX Runtime.

## 📋 Sobre o Projeto

ChurnInsight é uma aplicação que utiliza um modelo de Logistic Regression treinado com técnica SMOTE para prever a probabilidade de cancelamento (churn) de assinantes de serviços de música. A API recebe dados comportamentais do usuário e retorna a probabilidade de churn em tempo real.

### Características Principais

- ✅ **Inferência em tempo real** usando ONNX Runtime
- ✅ **Arquitetura Hexagonal** (Ports & Adapters)
- ✅ **Cache inteligente** com Caffeine
- ✅ **Rate Limiting** por IP/usuário
- ✅ **Métricas** via Actuator/Prometheus
- ✅ **Health Check** personalizado para o modelo
- ✅ **Histórico de predições** persistido em MySQL
- ✅ **Segurança** com Spring Security (HTTP Basic)
- ✅ **Containerização** com Docker

---

## 🏗️ Arquitetura

O projeto segue os princípios da **Arquitetura Hexagonal**:

```
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE ENTRADA                       │
│  ┌─────────────────┐          ┌──────────────────┐          │
│  │ REST Controller │          │  Rate Limiter    │          │
│  │   /predict      │  ──────► │   Filter         │          │
│  │   /stats        │          └──────────────────┘          │
│  └─────────────────┘                                         │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   CAMADA DE APLICAÇÃO                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │       ChurnPredictionService (Use Cases)             │   │
│  │  • PredictChurnUseCase                               │   │
│  │  • PredictionStatsUseCase                            │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE DOMÍNIO                       │
│  ┌──────────────────┐    ┌─────────────────────┐           │
│  │ CustomerProfile  │    │  ChurnStatus (Enum) │           │
│  │  (Value Object)  │    │  • WILL_CHURN       │           │
│  └──────────────────┘    │  • WILL_STAY        │           │
│                          └─────────────────────┘           │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   CAMADA DE INFRAESTRUTURA                   │
│  ┌──────────────────┐           ┌─────────────────────┐     │
│  │ OnnxRuntimeAdapter│          │ MySQLHistoryAdapter │     │
│  │  (Inferência ML) │          │  (Persistência)     │     │
│  └──────────────────┘           └─────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Tecnologias

### Core
- **Java 21**
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

### Utilitários
- **Lombok** (redução de boilerplate)
- **dotenv-java 3.2.0** (gerenciamento de variáveis de ambiente)
- **Univocity Parsers 2.9.1** (processamento de dados)

### Containerização
- **Docker** + **Docker Compose**
- **Multi-stage build** para otimização de imagem

---

## 📊 Métricas do Modelo

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
    "model": {
      "status": "UP",
      "details": {
        "status": "Modelo ONNX carregado com sucesso",
        "session": "Ativa"
      }
    }
  }
}
```

---

## 📡 Endpoints da API

### 🔐 Autenticação

Todos os endpoints (exceto `/actuator/health`) requerem **HTTP Basic Authentication**.

Adicione o header:
```
Authorization: Basic base64(username:password)
```

### 1. Predição Simples

**POST** `/predict`

Realiza uma predição e retorna apenas o resultado final.

**Request Body:**
```json
{
  "gender": "Male",
  "age": 28,
  "country": "BR",
  "subscriptionType": "Premium",
  "listeningTime": 120.5,
  "songsPlayedPerDay": 45,
  "skipRate": 0.35,
  "adsListenedPerWeek": 0,
  "deviceType": "Mobile",
  "offlineListening": true,
  "userId": "user-123-abc"
}
```

**Response:**
```json
{
  "label": "WILL_STAY",
  "probability": 0.3245
}
```

### 2. Predição com Estatísticas

**POST** `/stats`

Retorna predição com probabilidades detalhadas de cada classe.

**Request Body:** (mesmo formato do `/predict`)

**Response:**
```json
{
  "label": "WILL_STAY",
  "probability": 0.3245,
  "probabilities": [0.3245, 0.6755],
  "classProbabilities": {
    "WILL_CHURN": 0.3245,
    "WILL_STAY": 0.6755
  }
}
```

### 3. Métricas (Prometheus)

**GET** `/actuator/metrics`

Retorna métricas detalhadas da aplicação.

**GET** `/actuator/prometheus`

Retorna métricas no formato Prometheus.

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
│   ├── port/
│   │   ├── input/          # Use Cases (interfaces)
│   │   └── output/         # Ports para adapters
│   └── service/            # Implementação dos Use Cases
├── domain/
│   ├── enums/              # ChurnStatus
│   ├── exception/          # Exceções de domínio
│   └── model/              # CustomerProfile (Value Object)
├── infra/
│   ├── adapter/
│   │   ├── input/web/      # Controllers REST
│   │   └── output/
│   │       ├── inference/  # OnnxRuntimeAdapter
│   │       └── persistence/ # MySQL Adapter
│   ├── config/             # Configurações Spring
│   ├── exception/          # Global Exception Handler
│   ├── filter/             # Rate Limiting Filter
│   └── util/               # Utilitários
└── ChurnInsightApplication.java

src/main/resources/
├── db.migration/           # Scripts Flyway
│   └── V1__init_churn_history.sql
├── metadata.json           # Metadados do modelo
├── modelo_hackathon.onnx   # Modelo ONNX
└── application.properties
```

---

## 🔧 Configurações Avançadas

### Cache (application.properties)

```properties
app.cache.ttl-minutes=15
app.cache.max-size=20000
```

### Rate Limiting

```properties
app.rate-limit.requests-per-second=50
app.rate-limit.burst-capacity=100
```

### JVM Tuning (Dockerfile)

O container está configurado com:
- **ZGC (Z Garbage Collector)** para baixa latência
- **MaxRAMPercentage=75%** (usa 75% da RAM do container)

---

## 📈 Monitoramento

### Métricas Customizadas

- `houseprice.predictions.total` - Total de predições realizadas
- `houseprice.prediction.latency` - Latência de inferência (p50, p95, p99)
- `houseprice.requests.active` - Requisições ativas no momento
- `houseprice.errors.total` - Total de erros acumulados

### Health Check Customizado

O endpoint `/actuator/health` verifica:
- ✅ Status do banco de dados
- ✅ Modelo ONNX carregado
- ✅ Sessão ONNX ativa

---

## 🗄️ Schema do Banco de Dados

```sql
CREATE TABLE churn_history (
    id CHAR(36) PRIMARY KEY,
    
    -- Dados de entrada
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
    
    -- Auditoria
    requester_id CHAR(36),
    request_ip VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🧪 Testando a API

### Usando cURL

```bash
# Predição simples (com autenticação)
curl -X POST http://localhost:10808/predict \
  -u admin:sua_senha_aqui \
  -H "Content-Type: application/json" \
  -d '{
    "gender": "Female",
    "age": 32,
    "country": "US",
    "subscriptionType": "Free",
    "listeningTime": 45.2,
    "songsPlayedPerDay": 15,
    "skipRate": 0.65,
    "adsListenedPerWeek": 25,
    "deviceType": "Desktop",
    "offlineListening": false,
    "userId": "test-user-456"
  }'
```

### Usando Postman/Insomnia

1. Configure **Authorization → Basic Auth**
2. Username: valor de `SECURITY_USER` no `.env`
3. Password: valor de `SECURITY_PASSWORD` no `.env`
4. Body: JSON do CustomerProfile

---

## 🐛 Troubleshooting

### Erro: "Modelo ONNX não carregado"

**Solução:** Verifique se o arquivo `modelo_hackathon.onnx` está em `src/main/resources/`

### Erro: "Connection refused" ao MySQL

**Solução:** Aguarde o health check do MySQL:
```bash
docker-compose logs db
# Aguarde até ver: "ready for connections"
```

### Erro 429 (Too Many Requests)

**Solução:** Aguarde alguns segundos ou aumente o limite em `application.properties`

### Container da API não inicia

**Solução:** Verifique as variáveis de ambiente no `.env` e os logs:
```bash
docker-compose logs app
```

---

## 👥 Equipe

### Time Back-End 💻
- [**Ezandro Bueno**](https://github.com/ezbueno)
- [**Jorge Filipi Dias**](https://github.com/jorgefilipi)
- [**Wanderson Souza**](https://github.com/wandersondevops)
- [**Wendell Dorta**](https://github.com/WendellD3v)

### Time Data Science 📊
- [**André Ribeiro**](https://github.com/andrerochads)
- [**Kelly Muehlmann**](https://github.com/kellymuehlmann)
- **Luiz Alves**
- [**Mariana Fernandes**](https://github.com/mari-martins-fernandes)

---

## 📝 Licença

Este projeto foi desenvolvido para o **Hackathon DataBeats 2026**.

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

---

## 📧 Contato

Para dúvidas ou sugestões, abra uma issue no repositório ou entre em contato com a equipe.

---

**Desenvolvido com ❤️ usando Spring Boot e ONNX Runtime | Hackathon ONE 2026 - Equipe DataBeats**
