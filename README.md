# 📌 ChurnInsight – Back-end API

API REST desenvolvida em Java com Spring Boot para disponibilizar previsões de churn (cancelamento de clientes) para sistemas internos da empresa.

Este projeto faz parte do desafio **ChurnInsight**, cujo objetivo é prever se um cliente está propenso a cancelar um serviço recorrente.

---

## 🧠 Visão Geral

- O modelo de Data Science é responsável pela predição de churn.
- O back-end expõe uma API REST para consumo dessa predição.
- Atualmente, o projeto utiliza uma **implementação mock de predição**, apenas para simulação e testes do MVP.
- A API já está preparada para integração futura com o modelo real.

---

## 🚀 Tecnologias utilizadas

- Java 21
- Spring Boot V3.5.8
- Spring Web
- Spring Validation
- Spring Security (HTTP Basic Auth)
- Maven
- Lombok
- MySQL

---

## ⚙️ Configuração inicial

1. Clone o repositório

2. Copie o arquivo de exemplo:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

3. Edite o arquivo `application.properties` e configure suas credenciais de acesso:
```properties
spring.security.user.name=seu_usuario
spring.security.user.password=sua_senha
```

4. Execute o projeto:
```bash
mvn spring-boot:run
```

A aplicação ficará disponível em:
```
http://localhost:8080
```

> ⚠️ **Importante**: O arquivo `application.properties` não é versionado. Use variáveis de ambiente em produção.

---

## 🔐 Autenticação

A API utiliza **HTTP Basic Authentication** para proteger todos os endpoints.

Todas as requisições devem incluir as credenciais configuradas no `application.properties`.

---

## 🔗 Endpoints disponíveis

### ✅ POST `/predict`

Recebe informações do cliente e retorna a previsão de churn.

#### 📥 Requisição

```json
{
  "gender": "Female",
  "age": 25,
  "country": "Brazil",
  "subscription_type": "Basic",
  "listening_time": 4.5,
  "songs_played_per_day": 8,
  "skip_rate": 0.85,
  "device_type": "Mobile",
  "offline_listening": false
}

```

#### 📤 Resposta

```json
{
  "previsao": "Vai continuar",
  "probabilidade": 0.20
}
```

---

### ✅ GET `/stats`

Retorna estatísticas básicas das previsões realizadas.

#### 📤 Resposta

```json
{
  "total_avaliados": 3,
  "taxa_churn": 0.33
}
```

---

## 🧪 Como testar os endpoints

### 🔹 Usando cURL

#### POST `/predict`
```bash
curl -X POST http://localhost:8080/predict \
  -u seu_usuario:sua_senha \
  -H "Content-Type: application/json" \
  -d '{
    "tempo_contrato_meses": 12,
    "atrasos_pagamento": 1,
    "uso_mensal": 14.5,
    "plano": "Standard"
  }'
```

#### GET `/stats`
```bash
curl -X GET http://localhost:8080/stats \
  -u seu_usuario:sua_senha
```

---

### 🔹 Usando Postman/Insomnia

1. Selecione a aba **Authorization**
2. Escolha o tipo: **Basic Auth**
3. Preencha com as credenciais configuradas no `application.properties`
4. Envie a requisição normalmente

---

### 🔹 Usando Navegador (apenas GET)

Ao acessar `http://localhost:8080/stats`, o navegador solicitará as credenciais automaticamente.

---

## ⚠️ Validação de entrada

Caso algum campo obrigatório esteja ausente ou inválido, a API retorna erro 400:

```json
{
  "status": 400,
  "erro": "Erro de validação",
  "mensagens": [
    "O campo 'tempo_contrato_meses' é inválido ou obrigatório"
  ]
}
```

---

## ✅ Exemplos de Testes

### Cliente com alto risco

```json
{
  "tempo_contrato_meses": 3,
  "atrasos_pagamento": 4,
  "uso_mensal": 6.0,
  "plano": "Basic"
}
```

Resposta:

```json
{
  "previsao": "Vai cancelar",
  "probabilidade": 0.95
}
```

---

### Cliente com baixo risco

```json
{
  "tempo_contrato_meses": 36,
  "atrasos_pagamento": 0,
  "uso_mensal": 30.5,
  "plano": "Premium"
}
```

Resposta:

```json
{
  "previsao": "Vai continuar",
  "probabilidade": 0.01
}
```

---

## 🔄 Integração futura com Data Science

Quando o modelo real estiver pronto, a implementação mock será substituída por uma implementação real de predição, mantendo:

- Endpoints atuais
- Contrato JSON
- Validação
- Tratamento de erros

---

## ✅ Status do projeto

- ✅ MVP funcional
- ✅ API REST com autenticação
- ✅ Endpoints protegidos com HTTP Basic Auth
- ✅ Pronta para integração com Data Science
- ✅ Contrato definido e testado
