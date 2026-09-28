# User Service — Smart Patient Monitoring System

Handles: user registration (Admin creates users), login (Doctor/Nurse), JWT-based auth,
and role-based access control (ADMIN, DOCTOR, NURSE), matching the `user-service`
module in the project's architecture doc.

## Tech Stack
- Java 17, Spring Boot 3.3
- Spring Security 6 + JWT (JJWT)
- Spring Data JPA + PostgreSQL
- Lombok

## 1. Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL running locally (or update `application.properties`)

## 2. Create the database
```sql
CREATE DATABASE healthcare_users;
```
Tables are auto-created by Hibernate (`ddl-auto=update`) on first run — no manual DDL needed.

## 3. Configure
Edit `src/main/resources/application.properties` if your DB credentials differ:
```
spring.datasource.username=postgres
spring.datasource.password=postgres
```
In production, move `jwt.secret` and DB credentials to environment variables instead of
committing them in this file.

## 4. Run
```bash
mvn clean install
mvn spring-boot:run
```
Service starts on **http://localhost:8081**

## 5. API Endpoints

### Register a user (Admin creates users)
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "dr_harini",
  "password": "secret123",
  "email": "harini@hospital.com",
  "role": "DOCTOR"
}
```
Returns a JWT token + user info.

### Login (Doctor/Nurse login)
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "dr_harini",
  "password": "secret123"
}
```

### List all users (Admin only — requires Bearer token with ROLE_ADMIN)
```
GET /api/users
Authorization: Bearer <token>
```

### Get user by id / username
```
GET /api/users/{id}
GET /api/users/username/{username}
```

### Delete user (Admin only)
```
DELETE /api/users/{id}
```

## 6. Notes for integrating with the other services
- `vitals-service`, `alert-service`, and `analytics-service` should validate the same
  JWT (share the `jwt.secret`) so a single login token works across the whole system.
- `role` claim is embedded in the JWT so downstream services can do their own
  role checks without calling back into user-service.
