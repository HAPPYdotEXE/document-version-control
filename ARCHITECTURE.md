# Architecture Diagram

**Project:** SAP — Document Version Control System
**Purpose:** Full system overview for team reference. Covers layers, flows, and component responsibilities.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [System Layer Overview](#system-layer-overview)
3. [Layer Descriptions](#layer-descriptions)
4. [Authentication Flow](#authentication-flow)
5. [Document Creation Flow](#document-creation-flow)
6. [Version Upload & Review Flow](#version-upload--review-flow)
7. [Role & Access Control](#role--access-control)
8. [Component Quick Reference](#component-quick-reference)

---

## Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Web Layer | Spring MVC (REST) + Thymeleaf (server-side HTML) |
| Security | Spring Security + JWT (JSON Web Tokens) |
| Persistence | Spring Data JPA + Hibernate ORM |
| Database | H2 (in-memory, development) / PostgreSQL (production) |
| File Storage | Local filesystem (`uploads/` directory) |
| API Docs | SpringDoc OpenAPI 3.0 (Swagger UI) |
| Build Tool | Maven |
| Password Hashing | BCrypt |

---

## System Layer Overview

This diagram shows the full vertical stack — from the user's browser down to the database and file system.

```
╔══════════════════════════════════════════════════════════════════════╗
║                              CLIENT                                  ║
║                                                                      ║
║    Browser (Thymeleaf UI)              REST Client / Swagger UI      ║
║    ─────────────────────               ──────────────────────────    ║
║    Renders HTML pages served           Sends JSON requests to the    ║
║    by the server. Forms submit         API directly. Used by other   ║
║    POST/GET requests. Auth             services or via browser at    ║
║    state held in a JWT cookie.         /swagger-ui.html.             ║
║                                                                      ║
╠══════════════════════════════════════════════════════════════════════╣
║                          SECURITY LAYER                              ║
║                                                                      ║
║    Every request passes through this layer before reaching           ║
║    any controller. Handles authentication (who are you?) and         ║
║    authorization (what are you allowed to do?).                      ║
║                                                                      ║
║    JwtAuthenticationFilter → JwtUtil → UserDetailsServiceImpl        ║
║    SecurityConfig defines which routes are public vs protected.      ║
║                                                                      ║
╠══════════════════════════════════════════════════════════════════════╣
║                         CONTROLLER LAYER                             ║
║                                                                      ║
║    REST Controllers (JSON)         Page Controllers (HTML)           ║
║    ────────────────────────        ────────────────────────────      ║
║    AuthController                  AuthPageController                ║
║    DocumentController              DocumentPageController            ║
║    VersionController               HomeController                    ║
║    UserController                                                    ║
║    AuditLogController                                                ║
║                                                                      ║
║    Controllers receive requests, validate inputs, call services,     ║
║    and return responses. They contain no business logic.             ║
║                                                                      ║
╠══════════════════════════════════════════════════════════════════════╣
║                          SERVICE LAYER                               ║
║                                                                      ║
║    Contains all business logic and rules. Services interact          ║
║    with repositories to read/write data, with FileStorageService     ║
║    to handle files, and with AuditLogService to record actions.      ║
║                                                                      ║
║    AuthService       DocumentService      VersionService             ║
║    UserService       AuditLogService      FileStorageService         ║
║                                                                      ║
║    Utilities: EntityLookup · EntityBuilder · DtoMapper               ║
║                                                                      ║
╠══════════════════════════════════════════════════════════════════════╣
║                        REPOSITORY LAYER                              ║
║                                                                      ║
║    Spring Data JPA interfaces. No SQL written manually —             ║
║    Hibernate generates queries from method names and annotations.    ║
║                                                                      ║
║    UserRepository      RoleRepository       DocumentRepository       ║
║    VersionRepository   AuditLogRepository                            ║
║                                                                      ║
╠══════════════════════════════════════════════════════════════════════╣
║                 PERSISTENCE & STORAGE                                ║
║                                                                      ║
║    Database (H2 / PostgreSQL)          File System                   ║
║    ──────────────────────────          ──────────────────────        ║
║    Stores all entities:                Stores uploaded .txt files:   ║
║    users, roles, documents,            uploads/                      ║
║    versions, audit_logs                └── documents/                ║
║                                            └── {docId}/              ║
║                                                └── {versionNum}.txt  ║
╚══════════════════════════════════════════════════════════════════════╝
```

---

## Layer Descriptions

### Client
The application has two ways users interact with it:

- **Thymeleaf UI** — The server renders full HTML pages and sends them to the browser. The user navigates between pages (login, home, create document, view document). Authentication state is kept in an HTTP-only cookie named `jwt`.
- **REST API** — JSON-based API for programmatic access. Also exposed through Swagger UI at `/swagger-ui.html` for manual testing and documentation.

### Security Layer
Every single HTTP request passes through the security layer before reaching any controller.

- **`JwtAuthenticationFilter`** — Runs before each request. It looks for a JWT token in two places: the `Authorization: Bearer <token>` header (used by API clients) or the `jwt` cookie (used by the browser UI). If found, it validates the token.
- **`JwtUtil`** — Handles the cryptographic work: generating tokens on login and verifying the signature and expiry on each request.
- **`UserDetailsServiceImpl`** — Once the token is validated, this loads the full user record (including roles) from the database so Spring Security knows who is making the request.
- **`SecurityConfig`** — Defines the rules: which routes are public (no login needed) and which are protected. Role-level restrictions (e.g. only ADMIN can access `/api/v1/audit-logs`) are enforced via `@PreAuthorize` annotations on service methods.

**Public routes (no token needed):**
`/`, `/login`, `/register`, `/logout`, `/api/v1/auth/**`, `/h2-console/**`, `/swagger-ui.html`, static assets

**Protected routes (token required):**
Everything else under `/api/v1/**` and `/documents/**`

### Controller Layer
Controllers are the entry point for all requests. They:
1. Receive the HTTP request and extract parameters/body
2. Call the relevant service method
3. Return an HTTP response (JSON for REST, HTML redirect or model for Thymeleaf)

Controllers do **not** contain business logic. They delegate everything to services.

### Service Layer
This is where all business rules live. Key responsibilities:

- **`AuthService`** — Handles login (authenticates credentials, issues JWT) and registration (validates no duplicate username/email, hashes password, assigns role).
- **`DocumentService`** — Creates, reads, updates, and deletes documents. On creation, it also triggers the creation of the first version (which is auto-approved).
- **`VersionService`** — Manages the version lifecycle. Enforces rules like "only one version under review at a time" and "approving a version archives the previous active one".
- **`UserService`** — Manages user profiles. User deletion orphans their documents rather than deleting them, preserving history.
- **`AuditLogService`** — Called by other services after every state-changing action. Records who did what and when.
- **`FileStorageService`** — Validates that uploaded files are `.txt`, saves them to the filesystem, and can load or delete them.

**Utility classes used across services:**
- **`EntityLookup`** — Fetches entities from the DB by ID and resolves the currently authenticated user from the security context.
- **`EntityBuilder`** — Constructs new entities with correct initial state (e.g. sets status, timestamps).
- **`DtoMapper`** — Converts JPA entities to DTOs (Data Transfer Objects) for safe API responses. Handles null-safe conversion for orphaned records.

### Repository Layer
Spring Data JPA repositories. Each one corresponds to a database table. Services call these to read and write data. No raw SQL — Hibernate translates JPA method calls into queries.

### Database
- **H2** (default, development): In-memory database, resets on every restart. Data is seeded on startup via `data.sql` (inserts the 4 roles).
- **PostgreSQL** (production): Persistent relational database.

### File System
Uploaded document versions are stored as `.txt` files outside the database. Path pattern: `uploads/documents/{documentId}/{versionNumber}.txt`. The `file_path` column in the `versions` table points to the location on disk.

---

## Authentication Flow

This flow covers what happens from the moment a user submits their login credentials to every subsequent authenticated request.

```
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 1 — User submits login                                        │
│                                                                     │
│  POST /api/v1/auth/login                                            │
│  Body: { "username": "...", "password": "..." }                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 2 — AuthController receives request                           │
│                                                                     │
│  Passes credentials to AuthService.login()                          │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 3 — AuthService verifies credentials                          │
│                                                                     │
│  Spring's AuthenticationManager checks the username and             │
│  compares the submitted password against the BCrypt hash            │
│  stored in the database.                                            │
│                                                                     │
│  If credentials are wrong → 401 Unauthorized returned               │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Credentials valid
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 4 — JWT token generated                                       │
│                                                                     │
│  JwtUtil creates a signed token containing:                         │
│  - Subject: username                                                │
│  - Expiry: 24 hours (development setting)                           │
│  - Signed with HMAC-SHA256 secret key                               │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 5 — Token delivered to client                                 │
│                                                                     │
│  - Returned in the JSON response body (for API clients)             │
│  - Set as an HttpOnly cookie named "jwt" (for browser UI)           │
│                                                                     │
│  HttpOnly means JavaScript cannot read the cookie —                 │
│  protection against XSS attacks.                                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
          ┌────────────────────┘
          │  Every subsequent request
          ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 6 — JwtAuthenticationFilter intercepts the request            │
│                                                                     │
│  Looks for the token in:                                            │
│  1. Authorization header → "Bearer <token>"  (API clients)         │
│  2. Cookie named "jwt"                       (browser UI)           │
│                                                                     │
│  If no token found → request treated as anonymous                   │
│  If token found but expired/invalid → 401 Unauthorized              │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Token valid
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 7 — User identity established                                 │
│                                                                     │
│  UserDetailsServiceImpl loads the user and their roles from the DB. │
│  SecurityContext is populated with the authenticated user.          │
│  The request continues to the controller.                           │
│                                                                     │
│  Role-based checks (@PreAuthorize) fire at the service layer        │
│  when a method is called.                                           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Document Creation Flow

This flow covers what happens when an AUTHOR creates a new document.

```
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 1 — AUTHOR submits new document                               │
│                                                                     │
│  POST /api/v1/documents                                             │
│  Multipart form: name (text) + file (.txt)                          │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 2 — DocumentController                                        │
│                                                                     │
│  Receives the multipart request and calls                           │
│  DocumentService.createDocument(name, file)                         │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 3 — DocumentService validates and creates                     │
│                                                                     │
│  - Checks no other document with the same name exists               │
│    (names are globally unique) → 409 Conflict if duplicate          │
│  - EntityBuilder constructs the Document entity                     │
│  - Document saved to database via DocumentRepository                │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 4 — File saved to disk                                        │
│                                                                     │
│  FileStorageService:                                                │
│  - Validates the file is non-empty and is a .txt file               │
│  - Saves it to: uploads/documents/{documentId}/1.txt                │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 5 — Initial version created (auto-approved)                   │
│                                                                     │
│  A Version entity is created with:                                  │
│  - version_num = 1                                                  │
│  - status = APPROVED                                                │
│  - is_active = true                                                 │
│  - file_path = uploads/documents/{id}/1.txt                        │
│                                                                     │
│  This version is immediately the "live" version of the document.    │
│  No review step required for the initial upload.                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 6 — Audit log recorded                                        │
│                                                                     │
│  AuditLogService records two events:                                │
│  - DOCUMENT_CREATED  (entity: DOCUMENT, id: documentId)             │
│  - INITIAL_VERSION   (entity: VERSION,  id: versionId)              │
│                                                                     │
│  Both entries store: who did it, what action, timestamp.            │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Version Upload & Review Flow

This flow covers uploading a new version of an existing document and the subsequent review.

```
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 1 — AUTHOR uploads a new version                              │
│                                                                     │
│  POST /api/v1/documents/{documentId}/versions                       │
│  Multipart form: file (.txt)                                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 2 — VersionService validates the upload                       │
│                                                                     │
│  - File must be a non-empty .txt → 400 Bad Request if not           │
│  - No other version of this document may already be UNDER_REVIEW    │
│    → 409 Conflict if one exists (one review at a time per document) │
│  - Document must exist → 404 Not Found if it doesn't               │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Validation passed
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 3 — File saved, version created                               │
│                                                                     │
│  - FileStorageService saves file →                                  │
│    uploads/documents/{documentId}/{newVersionNum}.txt               │
│  - New Version entity created with:                                 │
│    status = UNDER_REVIEW, is_active = false                         │
│  - AuditLog: VERSION_UPLOADED                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
               ┌───────────────┴──────────────────┐
               │                                  │
               ▼                                  ▼
┌──────────────────────────┐       ┌──────────────────────────────────┐
│  REVIEWER approves       │       │  REVIEWER rejects                │
│                          │       │                                  │
│  PUT .../approve         │       │  PUT .../reject                  │
│  Body: { "comment": "" } │       │  Body: { "comment": "" }         │
└─────────────┬────────────┘       └────────────────┬─────────────────┘
              │                                     │
              ▼                                     ▼
┌──────────────────────────┐       ┌──────────────────────────────────┐
│  VersionService.approve()│       │  VersionService.reject()         │
│                          │       │                                  │
│  1. Previous active      │       │  1. This version →               │
│     version →            │       │     status = REJECTED            │
│     status = ARCHIVED    │       │     is_active = false            │
│     is_active = false    │       │                                  │
│                          │       │  2. Previous active version      │
│  2. This version →       │       │     remains APPROVED + active    │
│     status = APPROVED    │       │     (document still accessible)  │
│     is_active = true     │       │                                  │
│     review_comment saved │       │  3. review_comment saved         │
│     reviewed_by = User   │       │     reviewed_by = User           │
│                          │       │                                  │
│  3. AuditLog:            │       │  4. AuditLog:                    │
│     VERSION_APPROVED     │       │     VERSION_REJECTED             │
└──────────────────────────┘       └──────────────────────────────────┘

─────────────────────────────────────────────────────────────────────

Version status summary at any point in time:

  Document "Policy v3"
  ├── Version 1 — ARCHIVED   (was approved, superseded by v2)
  ├── Version 2 — APPROVED   is_active = true  ← what READERs see
  └── Version 3 — UNDER_REVIEW                 ← pending review
```

---

## Role & Access Control

The application uses four roles. Each role grants cumulative permissions.

```
┌──────────────────────────────────────────────────────────────────────────┐
│  READER                                                                  │
│  - View the list of all documents                                        │
│  - Read the currently active (approved) version of a document            │
└──────────────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────┐
│  AUTHOR                                                                  │
│  - Everything READER can do                                              │
│  - Create new documents (with initial file upload)                       │
│  - Upload new versions of existing documents                             │
│  - Rename documents                                                      │
│  - Delete documents (removes all versions and files)                     │
└──────────────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────┐
│  REVIEWER                                                                │
│  - Everything READER can do                                              │
│  - Approve a version that is UNDER_REVIEW (with optional comment)        │
│  - Reject a version that is UNDER_REVIEW (with optional comment)         │
└──────────────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────┐
│  ADMIN                                                                   │
│  - Everything AUTHOR and REVIEWER can do                                 │
│  - View all registered users                                             │
│  - Delete any user account                                               │
│  - View the full audit log (all actions, filterable by type or user)     │
└──────────────────────────────────────────────────────────────────────────┘
```

**How access control is enforced:**
- `SecurityConfig` blocks unauthenticated access to protected routes
- `@PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")` annotations on service methods enforce role checks at runtime
- If a user tries to call a method they don't have permission for → Spring Security throws `AccessDeniedException` → `GlobalExceptionHandler` returns 403 Forbidden

---

## Component Quick Reference

| Component | Package | Responsibility |
|---|---|---|
| `SecurityConfig` | `security` | Defines filter chain, public vs protected routes, CSRF settings |
| `JwtAuthenticationFilter` | `security` | Intercepts every request, reads and validates JWT |
| `JwtUtil` | `security` | Generates tokens on login, validates tokens on request |
| `UserDetailsServiceImpl` | `security` | Loads user + roles from DB for Spring Security |
| `AuthController` | `controller` | REST endpoints for login and register |
| `DocumentController` | `controller` | REST endpoints for document CRUD |
| `VersionController` | `controller` | REST endpoints for version upload, approve, reject, download |
| `UserController` | `controller` | REST endpoints for user profile and admin user management |
| `AuditLogController` | `controller` | REST endpoints for audit log queries (admin only) |
| `AuthPageController` | `controller` | Serves login and register HTML pages |
| `DocumentPageController` | `controller` | Serves create, view, delete document HTML pages |
| `HomeController` | `controller` | Serves the home page |
| `AuthService` | `service` | Login and registration business logic |
| `DocumentService` | `service` | Document CRUD + triggers version creation on document creation |
| `VersionService` | `service` | Version upload, approval, rejection, archiving, file reading |
| `UserService` | `service` | User profile read, password update, account deletion |
| `AuditLogService` | `service` | Records and queries audit events |
| `FileStorageService` | `service` | Validates, saves, loads, and deletes `.txt` files on disk |
| `EntityLookup` | `service/util` | DB lookups by ID + resolves current authenticated user from context |
| `EntityBuilder` | `service/util` | Constructs new entity instances with correct initial state |
| `DtoMapper` | `service/util` | Converts entities to response DTOs (null-safe for orphaned records) |
| `UserReferenceUtil` | `service/util` | Clears user foreign keys on deleted users without cascading |
| `GlobalExceptionHandler` | `exception` | Catches exceptions and returns structured HTTP error responses |
