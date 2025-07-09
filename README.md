# eBanking2 Transaction Service

A modern banking backend built with Java and Spring Boot. This service provides RESTful APIs for managing accounts, customers, and transactions, with robust API modeling using OpenAPI/Swagger.

---

## Features

- **Account, Customer, and Transaction Management**
- **RESTful API** with comprehensive OpenAPI (Swagger) documentation
- **JWT-based Authentication**
- **Kubernetes & Docker**
- **Extensive Integration and Unit Tests**

---

## Technology Stack

- Java 17+
- Spring Boot 3.2+
- Spring Security (JWT)
- Spring Kafka
- Springdoc OpenAPI
- Lombok
- Docker
- Kubernetes

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker (for containerization)
- (Optional) Kubernetes (for orchestration)

## API Documentation

Once running, access the interactive API docs at:

- `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `/v3/api-docs`

---

## Key API Endpoints

### Transactions

- **Create Transaction**
  - `POST /api/v1/transactions`
  - Request body: `TransactionRequestDto`
  - Response: `TransactionResponseDto`

- **Get Transactions by Account**
  - `GET /api/v1/transactions/account/{iban}`
  - Response: `AccountTransactionsSummaryDto`  
    Includes:
    - `transactions`: List of transactions
    - `totalDebit`: Total debit amount
    - `totalCredit`: Total credit amount

- **Get Transactions by Customer**
  - `GET /api/v1/transactions/customer/{customerId}`
  - Response: List of `TransactionResponseDto`

- **Add Transaction**
  - `POST /api/v1/addTransaction`
  - Parameters: amount, currency, accountIban, description, valueDate
  - Response: `AddTransactionResponse`

### Authentication

- **Login**
  - `POST /api/v1/auth/login`
  - Request body: `LoginRequest`
  - Response: `LoginResponse` (JWT token)

- **Register**
  - `POST /api/v1/auth/register`
  - Request body: `RegisterRequest`
  - Response: Success message

### Error Handling

All error responses follow the `ErrorResponse` schema, including status, error, message, path, timestamp, and optional validation errors.

---

## DTOs (Data Transfer Objects)

### TransactionRequestDto
- `accountIban` (String, required)
- `amount` (BigDecimal, required)
- `description` (String, required)
- `valueDate` (LocalDate, required)

### TransactionResponseDto
- `id` (String)
- `accountIban` (String)
- `currency` (String)
- `amount` (BigDecimal)
- `description` (String)
- `valueDate` (LocalDate)

### AccountTransactionsSummaryDto
- `transactions` (List<TransactionResponseDto>)
- `totalDebit` (BigDecimal)
- `totalCredit` (BigDecimal)

### AddTransactionResponse
- `status` (String)
- `message` (String)
- `transactionId` (String)
- `customerId` (String)

### LoginRequest
- `customerId` (String, required)
- `password` (String, required)

### LoginResponse
- `token` (String)
- `customerId` (String)
- `message` (String)

### RegisterRequest
- `customerId` (String, required)
- `password` (String, required, min 6 chars)

### ErrorResponse
- `status` (int)
- `error` (String)
- `message` (String)
- `path` (String)
- `timestamp` (LocalDateTime)
- `correlationId` (String, optional)
- `validationErrors` (List<ValidationError>, optional)

---

## Configuration

See `src/main/resources/application.yml` for all configuration options, including Kafka, JWT, and exchange rate API settings.

---

## Testing

- **Unit and Integration Tests:**
  ```sh
  mvn test
  ```