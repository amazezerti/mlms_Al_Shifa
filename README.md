# Al-Shifa MLMS — Medical Lab Management System

A role-based web application for **Al-Shifa Hospital, N'Djamena, Chad**.  
Manages patient registration, doctor assignments, lab test ordering, sample tracking, result verification, and billing — all in one system.

---

## Table of Contents

1. [Overview](#overview)
2. [Tech Stack](#tech-stack)
3. [Prerequisites](#prerequisites)
4. [Getting Started](#getting-started)
   - [1. Clone the repository](#1-clone-the-repository)
   - [2. Create the PostgreSQL database](#2-create-the-postgresql-database)
   - [3. Configure application.properties](#3-configure-applicationproperties)
   - [4. Run the application](#4-run-the-application)
5. [Default Login](#default-login)
6. [Role Overview](#role-overview)
7. [Project Structure](#project-structure)
8. [Database Migrations](#database-migrations)
9. [Common Issues](#common-issues)

---

## Overview

Al-Shifa MLMS covers the full clinical workflow:

```
Patient registered by Receptionist
       ↓
Assigned to doctor by department (load-balanced)
       ↓
Test order created by Receptionist → confirmed by Doctor
       ↓
Lab Technician collects sample → enters results
       ↓
Doctor verifies results → printed report
       ↓
Receptionist generates invoice → records payment
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 24 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 (BCrypt, session-based) |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 14+ |
| Migrations | Flyway |
| Templates | Thymeleaf 3.1 |
| Build | Maven (wrapper included) |
| UI | Bootstrap 5.3 + Bootstrap Icons |

---

## Prerequisites

Install these before you begin:

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 24 | https://adoptium.net |
| PostgreSQL | 14 or later | https://www.postgresql.org/download |
| Git | Any recent | https://git-scm.com |

> Maven does **not** need to be installed separately — the project includes `mvnw` / `mvnw.cmd` wrappers.

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/amazezerti/mlms_Al_Shifa.git
cd mlms_Al_Shifa
```

The Spring Boot project lives inside the inner `mlms-al-shifa/` folder:

```bash
cd mlms-al-shifa
```

> All commands from this point run from inside `mlms-al-shifa/`.

---

### 2. Create the PostgreSQL database

Open **psql** or any PostgreSQL client and run:

```sql
CREATE DATABASE mlms_db;
```

> The default setup expects a user `postgres` with access to `mlms_db`.  
> You can use any username — just update `application.properties` to match.

---

### 3. Configure application.properties

A template file is included. Copy it and fill in your values:

**Linux / macOS:**
```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

**Windows (PowerShell):**
```powershell
Copy-Item src\main\resources\application.properties.example `
          src\main\resources\application.properties
```

Now open `src/main/resources/application.properties` and set:

```properties
# ── Database connection ─────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5432/mlms_db
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD

# ── Admin account (created automatically on first run) ──
app.admin.username=admin
app.admin.password=YOUR_STRONG_ADMIN_PASSWORD
app.admin.fullname=System Administrator
app.admin.email=admin@alshifa.td
```

> `application.properties` is listed in `.gitignore` and will **never** be committed.

---

### 4. Run the application

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

The first run will:
1. Flyway runs all migrations (V1–V13) and creates every table
2. `DataInitializer` creates the admin user from your `application.properties`
3. The app starts at **http://localhost:8080**

You should see a line like this in the console when it's ready:
```
Started MlmsAlShifaApplication in X.XXX seconds
```

---

## Default Login

| Field | Value |
|-------|-------|
| URL | http://localhost:8080 |
| Username | value of `app.admin.username` in your properties file |
| Password | value of `app.admin.password` in your properties file |

After logging in as admin, use **User Management** to create accounts for Receptionists, Doctors, and Lab Technicians.

---

## Role Overview

| Role | Access | URL prefix |
|------|--------|------------|
| `ROLE_ADMIN` | User management, departments, doctor profiles, availability slots | `/admin/**` |
| `ROLE_RECEPTIONIST` | Patient registration, assignment, test orders, invoices, payments, print results | `/receptionist/**` |
| `ROLE_DOCTOR` | Confirm/reject test orders, verify results, view appointments | `/doctor/**` |
| `ROLE_LAB_TECHNICIAN` | Sample collection, result entry, results history | `/technician/**` |

### Recommended setup order after first login

1. **Admin → Departments** — create departments (e.g. Hematology, Biochemistry)
2. **Admin → Users** — create Doctor accounts
3. **Admin → Users → Doctor Profile** (person-badge icon) — assign each doctor to a department
4. **Admin → Users → Availability** (calendar icon) — add appointment slots for doctors
5. **Admin → Users** — create Receptionist and Lab Technician accounts
6. Log in as Receptionist to register patients and create test orders

---

## Project Structure

```
mlms-al-shifa/
├── src/main/java/com/alshifa/mlms_al_shifa/
│   ├── config/          # Security, data initializer
│   ├── controller/      # One controller per role + dashboard + error
│   ├── exception/       # Global exception handler
│   ├── model/           # JPA entities (User, Patient, TestOrder, …)
│   ├── repository/      # Spring Data JPA interfaces
│   └── service/         # Business logic and @Transactional boundaries
│
├── src/main/resources/
│   ├── db/migration/    # Flyway SQL migrations V1–V13
│   ├── static/          # Static assets (images)
│   ├── templates/       # Thymeleaf HTML templates
│   │   ├── admin/
│   │   ├── doctor/
│   │   ├── receptionist/
│   │   ├── technician/
│   │   ├── layout/      # base.html — shared master layout
│   │   └── error/       # 400, 403, 404, 500 pages
│   ├── application.properties          ← YOU CREATE THIS (gitignored)
│   └── application.properties.example ← safe template committed to git
│
└── pom.xml
```

---

## Database Migrations

Flyway automatically runs all migrations in order on startup.  
Migration files are in `src/main/resources/db/migration/`:

| File | Description |
|------|-------------|
| V1 | Departments table |
| V2 | Roles and Users tables |
| V3 | Patients table |
| V4 | Test catalog |
| V5 | Test orders and order items |
| V6 | Samples table |
| V7 | Test results |
| V8 | Invoices and payments |
| V9 | Inventory (reserved) |
| V10 | Seed admin user role |
| V11 | Doctor profiles and patient assignments |
| V12 | Seed test catalog (common lab tests) |
| V13 | Patient reference on availability slots |

> **Never modify a migration file that has already been applied.**  
> Add new changes as `V14__description.sql`, etc.

---

## Common Issues

**App fails to start — "relation does not exist"**  
→ The database `mlms_db` does not exist. Run `CREATE DATABASE mlms_db;` in psql.

**App fails to start — "password authentication failed"**  
→ Check `spring.datasource.username` and `spring.datasource.password` in your `application.properties`.

**App fails to start — "Schema-validation: missing column"**  
→ A migration may not have applied cleanly. Drop and recreate the database, then restart.

**`./mvnw` not recognized on Windows**  
→ Use `.\mvnw.cmd` instead of `./mvnw` in PowerShell.

**Login fails with correct credentials**  
→ Make sure the app started successfully at least once after you set `app.admin.password` — `DataInitializer` only creates the user on a clean database. If you changed the password after the user was already created, update it via Admin → Users → Reset Password.

---

## License

Internal project — Al-Shifa Hospital, N'Djamena, Republic of Chad.
