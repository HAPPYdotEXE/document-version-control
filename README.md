# SAP — Document Version Control System

A web application for managing documents through a structured upload, review, and approval workflow. Built with Spring Boot as a university project.

---

## What It Does

Teams often need a controlled process for publishing documents — someone writes it, someone else reviews it, and only approved versions are visible to readers. This application provides exactly that:

- **Authors** upload documents and new versions
- **Reviewers** approve or reject versions with a comment
- **Readers** view the currently approved version
- **Admins** manage users and view a full audit trail of every action

Every state change (upload, approve, reject, delete) is recorded in an immutable audit log.

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Web | Spring MVC (REST API) + Thymeleaf (server-rendered UI) |
| Security | Spring Security + JWT (stored in HttpOnly cookie) |
| Database | H2 in-memory (dev) / PostgreSQL (prod) |
| ORM | Spring Data JPA + Hibernate |
| File Storage | Local filesystem (`uploads/` directory) |
| API Docs | Swagger UI via SpringDoc OpenAPI |
| Build | Maven |

---

## Running Locally

### Prerequisites

- Java 21+
- Maven 3.8+

### Database configuration

The committed `src/main/resources/application.properties` is configured for **PostgreSQL** (the production database). If you run the application without changing anything, it will try to connect to a local PostgreSQL instance and fail if one is not running.

You have two options:

---

#### Option A — Use H2 (recommended for quick local development)

H2 is an in-memory database that requires no installation. To use it, open `src/main/resources/application.properties` and **replace** the datasource block with the following:

```properties
# ── Datasource — H2 in-memory (local development) ──
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# ── H2 browser console ──
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

> **Do not commit this change.** The PostgreSQL configuration is intentional for the shared/production setup. Keep your local H2 override only on your machine.

Once changed, run:

```bash
./mvnw spring-boot:run
```

The H2 database is **in-memory** — all data is wiped on every restart. The four roles are re-seeded automatically from `data.sql` on each boot.

**H2 browser console** (inspect tables while the app is running):

1. Go to `http://localhost:8081/h2-console`
2. Use these connection settings:
   - **JDBC URL:** `jdbc:h2:mem:testdb`
   - **Username:** `sa`
   - **Password:** `password`

---

#### Option B — Use PostgreSQL locally

1. Install PostgreSQL and create a database and user matching the properties file:

```sql
CREATE DATABASE document_version_control;
CREATE USER dvc_user WITH PASSWORD '1234';
GRANT ALL PRIVILEGES ON DATABASE document_version_control TO dvc_user;
```

2. Leave `application.properties` unchanged and run:

```bash
./mvnw spring-boot:run
```

Data persists across restarts with this option.

---

### Available URLs

| URL | Description |
|---|---|
| `http://localhost:8081` | Home page (Thymeleaf UI) |
| `http://localhost:8081/login` | Login page |
| `http://localhost:8081/register` | Register page |
| `http://localhost:8081/swagger-ui.html` | Interactive API documentation |
| `http://localhost:8081/h2-console` | H2 browser console (Option A only) |

---

## Default Data

On startup, `src/main/resources/data.sql` seeds the four roles:

| ID | Role |
|---|---|
| 1 | `READER` |
| 2 | `AUTHOR` |
| 3 | `REVIEWER` |
| 4 | `ADMIN` |

No users are pre-created. Register through `/register` and select a role.

---

## User Roles

| Role | What They Can Do |
|---|---|
| **READER** | View the active (approved) version of any document |
| **AUTHOR** | Create documents, upload new versions, rename and delete documents |
| **REVIEWER** | Approve or reject versions that are under review (with a comment) |
| **ADMIN** | Everything above + manage users + view audit logs |

---

## API Overview

Full interactive documentation is available at `/swagger-ui.html` once the app is running.

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Login and receive a JWT token |

### Documents
| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| GET | `/api/v1/documents` | Any | List all documents |
| GET | `/api/v1/documents/{id}` | Any | Get a document by ID |
| POST | `/api/v1/documents` | AUTHOR / ADMIN | Create a document (multipart: name + file) |
| PUT | `/api/v1/documents/{id}` | AUTHOR / ADMIN | Rename a document |
| DELETE | `/api/v1/documents/{id}` | AUTHOR / ADMIN | Delete a document and all its versions |

### Versions
| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| GET | `/api/v1/documents/{id}/versions` | Any | Get version history |
| GET | `/api/v1/documents/{id}/versions/active` | Any | Get the current active version |
| POST | `/api/v1/documents/{id}/versions` | AUTHOR / ADMIN | Upload a new version (multipart: file) |
| PUT | `/api/v1/documents/{id}/versions/{n}/approve` | REVIEWER / ADMIN | Approve a version |
| PUT | `/api/v1/documents/{id}/versions/{n}/reject` | REVIEWER / ADMIN | Reject a version |
| GET | `/api/v1/documents/{id}/versions/{n}/file` | Any | Download the version file |

### Users
| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| GET | `/api/v1/users/me` | Any | Get your own profile |
| PUT | `/api/v1/users` | Any | Update your password |
| DELETE | `/api/v1/users/me` | Any | Delete your own account |
| GET | `/api/v1/users` | ADMIN | List all users |
| GET | `/api/v1/users/{id}` | ADMIN | Get a user by ID |
| DELETE | `/api/v1/users/{id}` | ADMIN | Delete a user by ID |

### Audit Logs
| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| GET | `/api/v1/audit-logs` | ADMIN | Get all audit events |
| GET | `/api/v1/audit-logs/{entityType}` | ADMIN | Filter by entity type (USER / DOCUMENT / VERSION) |
| GET | `/api/v1/audit-logs/user/{userId}` | ADMIN | Filter by the user who performed the action |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/project/practice/sap/
│   │   ├── SapApplication.java         # Entry point, enables JPA Auditing
│   │   ├── config/
│   │   │   └── OpenApiConfig.java      # Swagger / OpenAPI configuration
│   │   ├── controller/                 # REST and Thymeleaf page controllers
│   │   ├── model/                      # JPA entities and enums
│   │   ├── dto/                        # Request and response data transfer objects
│   │   ├── service/                    # Business logic
│   │   │   └── util/                   # DtoMapper, EntityLookup, EntityBuilder
│   │   ├── repository/                 # Spring Data JPA interfaces
│   │   ├── security/                   # JWT filter, config, utilities
│   │   └── exception/                  # Custom exceptions and global handler
│   └── resources/
│       ├── application.properties      # App configuration
│       ├── data.sql                    # Role seed data
│       ├── templates/                  # Thymeleaf HTML templates
│       └── static/                     # CSS, JS, images
└── test/
    └── java/com/project/practice/sap/
        └── SapApplicationTests.java    # Application context load test
```

---

## Documentation

| File | Contents |
|---|---|
| `ARCHITECTURE.md` | System architecture, request flows, and component reference |

---

## Key Design Decisions

**JWT in HttpOnly cookie** — The browser UI stores the token in a cookie so JavaScript cannot access it, reducing XSS risk. API clients can use the `Authorization: Bearer` header instead.

**User deletion orphans documents** — When a user is deleted, their documents and versions are kept with a null `created_by` reference. This preserves document history and prevents data loss.

**File storage on disk** — Uploaded `.txt` files are stored on the local filesystem rather than in the database, keeping the DB lean. The `file_path` column in `versions` points to the file location.

**One active version per document** — The `is_active` flag ensures there is always at most one live version. Approving a new version automatically archives the previous one.

**Append-only audit log** — The `audit_logs` table is never updated or deleted. It provides a permanent record of all actions for compliance and debugging.
