# Money Transfer System (Backend)

## Overview
This is the backend for the Enterprise Money Transfer System, built with Java 17 and Spring Boot 3.2. It provides REST APIs for user authentication, account management, and money transfers with idempotency and transaction logging.

## Prerequisites
- **Java 17** or higher
- **MySQL 8.0+** running on localhost:3306
- **Git**

## Setup Instructions

### 1. Database Setup
Create the database before running the application:
```sql
CREATE DATABASE money_transfer_db;
```
*Note: The application is configured to use `root/root`. Update `src/main/resources/application.yml` if your credentials differ.*

### 2. Configuration
The application uses `update` strategy for DDL, so it will automatically create tables on startup.
To load initial demo data (users/accounts), you can change `spring.sql.init.mode` to `always` in `application.yml` for the first run, or manually execute `src/main/resources/data.sql`.

## Running the Application

### Using Gradle Wrapper (Recommended)
Open a terminal in the project root:
```bash
./gradlew bootRun
```
The server will start on `http://localhost:8080`.

## Running Tests
To run the full JUnit test suite (Unit + Integration):
```bash
./gradlew test
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/login?username=alice` (Basic Auth defaults: password is usually distinct per user, e.g., `alice123`)

### Accounts
- `GET /api/v1/accounts/{id}` - Get details
- `GET /api/v1/accounts/{id}/balance` - Get balance
- `GET /api/v1/accounts/{id}/transactions` - Get history

### Transfers
- `POST /api/v1/transfers` - JSON Body: `{"fromAccountId": 1, "toAccountId": 2, "amount": 50.00, "idempotencyKey": "unique-uuid"}`
- `GET /api/v1/transfers` - Get user's transfer history
- `GET /api/v1/transfers/all` - Admin only history

## Security
- **Admin**: `admin / admin123`
- **Users**: `user / user123`, `alice / alice123`, `bob / bob123`, etc.
