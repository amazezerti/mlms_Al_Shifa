# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MLMS Al-Shifa is a Medical Lab Management System for Al-Shifa Hospital (N'Djamena). It is a role-based Spring Boot MVC web application managing patient registration, doctor assignments, lab test ordering, sample tracking, and billing.

## Commands

The project root contains an inner directory `mlms-al-shifa/`. Run all commands from there:

```bash
cd mlms-al-shifa

# Run in development (hot reload via DevTools)
./mvnw spring-boot:run

# Build
./mvnw clean install

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Package as JAR
./mvnw clean package
java -jar target/mlms-al-shifa-0.0.1-SNAPSHOT.jar
```

App runs at `http://localhost:8080`. Default admin credentials are set in `application.properties` (copy from `application.properties.example`).

## Tech Stack

- **Java 24**, Spring Boot 3.5.13, Maven
- **PostgreSQL** on `localhost:5432`, database `mlms_db` (user: `postgres`, pass: `1234`)
- **Flyway** for migrations (`src/main/resources/db/migration/`, V1–V11)
- **Thymeleaf** for server-side HTML rendering
- **Spring Security 6** with BCrypt (strength 12), form login, session max=1, 30-min expiry
- **Lombok** for boilerplate reduction
- **Spring Boot Actuator** (only `/health` and `/info` exposed)

## Architecture

### Layered MVC

```
controller/ → service/ → repository/ → model/
```

- **Controllers**: 5 role-specific + DashboardController + CustomErrorController
- **Services**: Business logic and `@Transactional` boundaries
- **Repositories**: Spring Data JPA interfaces (auto-implemented)
- **Models**: 8 JPA entities

### Role-Based Access Control

| Role | Controller | URL Prefix |
|------|-----------|------------|
| ROLE_ADMIN | AdminController | `/admin/**` |
| ROLE_RECEPTIONIST | ReceptionistController | `/receptionist/**` |
| ROLE_DOCTOR | DoctorController | `/doctor/**` |
| ROLE_LAB_TECHNICIAN | TechnicianController | `/technician/**` |

`SecurityConfig.java` defines these URL-to-role mappings. `DataInitializer` seeds the default admin user on startup.

### Key Domain Entities

- **User** + **Role** (ManyToMany, EAGER) — authentication and authorization
- **DoctorProfile** → User (OneToOne), Department (ManyToOne, EAGER)
- **DoctorAvailability** — time slots per doctor (date, time range, booked flag)
- **PatientAssignment** → Patient + Doctor (User) + Department — load-balanced assignment via `AppointmentService`
- **Patient** — unique code, demographics, linked to User

### Template Layout

Thymeleaf templates in `src/main/resources/templates/` are organized by role (`admin/`, `doctor/`, `receptionist/`, `technician/`). `layout/base.html` is the shared master layout with role-based navigation.

### Notable Patterns

- **Load balancing**: `AppointmentService` assigns patients to least-loaded doctors
- **Custom auth failure**: `CustomAuthenticationFailureHandler` returns user-friendly login errors
- **Global exceptions**: `GlobalExceptionHandler` handles controller-level exceptions
- **Custom error pages**: 400, 403, 404, 500 under `templates/error/`
- **JPA fetch strategy**: Roles are EAGER (required for Spring Security); other relations are LAZY

### Database Migrations

Flyway migrations are numbered V1–V11. Add new migrations as `V12__description.sql`, etc. Do not modify existing migration files.
