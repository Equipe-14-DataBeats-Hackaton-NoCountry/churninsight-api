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

- Java 25
- Spring Boot V4
- Spring Web
- Spring Validation
- Maven
- Lombok
- (Opcional) H2 / PostgreSQL

---

## 🔗 Endpoints disponíveis

### ✅ POST `/predict`

Recebe informações do cliente e retorna a previsão de churn.

#### 📥 Requisição

```json
{
  "tempo_contrato_meses": 12,
  "atrasos_pagamento": 1,
  "uso_mensal": 14.5,
  "plano": "Standard"
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

## ▶️ Como executar o projeto

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em:

```
http://localhost:8080
```

---

## ✅ Status do projeto

- MVP funcional
- API pronta para integração com Data Science
- Endpoints testados
- Contrato definido
