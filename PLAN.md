# PLAN.md — Phased Roadmap & Progress Tracker

## Part A: CI/CD Foundation (COMPLETED)

> Phases 1-6 built the CI/CD pipeline. All complete and deployed.
> See LEARNING.md for detailed notes on each phase.

| Phase | Branch | What | Status |
|-------|--------|------|--------|
| 1 | `feature/01-spring-boot-app` | Spring Boot app + REST API + tests | Done |
| 2 | `feature/02-basic-ci-workflow` | GitHub Actions CI (auto build/test) | Done |
| 3 | `feature/03-manual-cf-deploy` | Manual CF deploy + secrets + auth fix | Done |
| 4 | `feature/04-terraform-cf-infra` | Terraform IaC for CF resources | Done |
| 5 | `feature/05-full-cicd-pipeline` | 3-job pipeline, TF plan on PR, deploy on merge | Done |
| 6 | `feature/06-advanced-workflows` | Reusable workflows, matrix builds, badge | Done |

---

## Part B: Full-Stack Task Manager (IN PROGRESS)

Extend the Spring Boot app into a full-stack application with Angular frontend,
OpenAPI documentation, and Keycloak authentication.

### Architecture

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Angular    │      │  Spring Boot │      │   Keycloak   │
│  (Frontend)  │─────>│  (REST API)  │      │   (Docker)   │
│              │ HTTP │              │      │              │
│ Port: 4200   │      │ Port: 8080   │      │ Port: 8180   │
└──────┬───────┘      └──────┬───────┘      └──────┬───────┘
       │                     │                     │
       │  1. User clicks     │                     │
       │     "Login"         │                     │
       │─────────────────────┼────────────────────>│
       │                     │                     │
       │  2. Keycloak shows  │                     │
       │     login page      │                     │
       │<────────────────────┼─────────────────────│
       │                     │                     │
       │  3. User enters     │                     │
       │     credentials     │                     │
       │─────────────────────┼────────────────────>│
       │                     │                     │
       │  4. Keycloak returns│                     │
       │     JWT token       │                     │
       │<────────────────────┼─────────────────────│
       │                     │                     │
       │  5. Angular sends   │                     │
       │     API request     │                     │
       │     + Bearer token  │                     │
       │────────────────────>│                     │
       │                     │  6. Validate JWT    │
       │                     │     (public key)    │
       │                     │────────────────────>│
       │                     │                     │
       │  7. API response    │                     │
       │<────────────────────│                     │
       │                     │                     │

Database: H2 (in-memory) → PostgreSQL (later)
```

### Tech Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Backend | Spring Boot 3.x | REST API, JPA, validation |
| Frontend | Angular 17+ | SPA, reactive forms, routing |
| API Docs | OpenAPI 3.0 (springdoc) | Auto-generated spec + Swagger UI |
| Auth | Keycloak (Docker) | OAuth2/OIDC identity provider |
| Auth Protocol | OAuth2 + OIDC + PKCE | Secure token-based authentication |
| Backend Security | spring-boot-starter-oauth2-resource-server | JWT validation |
| Frontend Auth | angular-oauth2-oidc | OIDC login flow with PKCE |
| Database | H2 → PostgreSQL | Start simple, upgrade later |
| Containerization | Docker Compose | Run Keycloak + DB locally |

---

## Phase 7: Task CRUD API + OpenAPI

**Branch:** `feature/07-task-crud-api`
**Goal:** Extend the Spring Boot app with a proper Task entity, CRUD endpoints,
and auto-generated OpenAPI documentation with Swagger UI.

### Data Model

```
Task
├── id: Long (auto-generated)
├── title: String (required)
├── description: String (optional)
├── status: Enum (TODO, IN_PROGRESS, DONE)
├── priority: Enum (LOW, MEDIUM, HIGH)
├── dueDate: LocalDate (optional)
├── createdAt: LocalDateTime (auto-set)
└── updatedAt: LocalDateTime (auto-set)
```

### API Endpoints

```
GET    /api/tasks          List all tasks
GET    /api/tasks/{id}     Get task by ID
POST   /api/tasks          Create a new task
PUT    /api/tasks/{id}     Update a task
DELETE /api/tasks/{id}     Delete a task
```

### Files

- `pom.xml` — add springdoc-openapi, H2, Spring Data JPA dependencies
- `src/.../model/Task.java` — JPA entity
- `src/.../model/TaskStatus.java` — Enum (TODO, IN_PROGRESS, DONE)
- `src/.../model/TaskPriority.java` — Enum (LOW, MEDIUM, HIGH)
- `src/.../repository/TaskRepository.java` — Spring Data JPA repository
- `src/.../service/TaskService.java` — Business logic layer
- `src/.../controller/TaskController.java` — REST controller with OpenAPI annotations
- `src/.../dto/TaskRequest.java` — Request DTO (what the client sends)
- `src/.../dto/TaskResponse.java` — Response DTO (what the API returns)
- `src/.../exception/TaskNotFoundException.java` — Custom exception
- `src/.../exception/GlobalExceptionHandler.java` — @ControllerAdvice
- `src/main/resources/application.yml` — H2 + OpenAPI config
- `src/test/...` — Controller + service tests

### What You Learn

- JPA entities with `@Entity`, `@Id`, `@GeneratedValue`, `@Enumerated`
- Spring Data JPA repository (zero-implementation CRUD)
- Service layer pattern (controller → service → repository)
- DTO pattern (separate API shape from database shape)
- springdoc-openapi: auto-generates OpenAPI 3.0 spec from code
- Swagger UI: interactive API explorer at `/swagger-ui.html`
- OpenAPI annotations: `@Operation`, `@ApiResponse`, `@Schema`
- H2 console for browsing the in-memory database
- Global exception handling with `@ControllerAdvice`
- `ResponseEntity` for proper HTTP status codes (201 Created, 404 Not Found)

### Checklist

- [ ] H2 + JPA + springdoc dependencies added to pom.xml
- [ ] Task entity with enums created
- [ ] TaskRepository interface created
- [ ] TaskService with CRUD operations created
- [ ] TaskController with all 5 endpoints created
- [ ] DTOs (TaskRequest, TaskResponse) created
- [ ] Global exception handler returns proper error responses
- [ ] Swagger UI accessible at /swagger-ui.html
- [ ] OpenAPI spec at /v3/api-docs shows all endpoints
- [ ] H2 console accessible at /h2-console (dev only)
- [ ] Unit tests for controller and service
- [ ] Existing /api/hello endpoint still works
- [ ] CI pipeline passes

---

## Phase 8: Keycloak + Spring Security

**Branch:** `feature/08-keycloak-security`
**Goal:** Set up Keycloak via Docker and secure the API with OAuth2/JWT.

### Keycloak Configuration

```
Keycloak (Docker)
└── Realm: task-manager
    ├── Client: task-manager-api (backend — confidential)
    │   └── Service account: validates tokens
    ├── Client: task-manager-web (frontend — public, PKCE)
    │   └── Redirect URI: http://localhost:4200/*
    ├── Realm Roles:
    │   ├── ROLE_ADMIN — can manage all tasks + users
    │   └── ROLE_USER — can manage own tasks only
    └── Test Users:
        ├── admin / admin123 (ROLE_ADMIN)
        └── user1 / user123 (ROLE_USER)
```

### Files

- `docker-compose.yml` — Keycloak + (optional) PostgreSQL for Keycloak
- `keycloak/realm-export.json` — Pre-configured realm (importable)
- `pom.xml` — add spring-boot-starter-oauth2-resource-server
- `src/.../config/SecurityConfig.java` — JWT validation, role mapping, CORS
- `src/main/resources/application.yml` — Keycloak issuer-uri, JWKS config
- Update `TaskController.java` — `@PreAuthorize` role checks
- Update `TaskService.java` — filter tasks by user (from JWT `sub` claim)
- `src/.../model/Task.java` — add `userId` field
- Swagger UI config — OAuth2 "Authorize" button

### What You Learn

- OAuth2 / OpenID Connect protocol (authorization code flow + PKCE)
- Keycloak concepts: realms, clients, roles, users
- Docker Compose for local development infrastructure
- Spring Security as OAuth2 Resource Server (validates JWTs)
- JWT anatomy: header, payload (claims), signature
- `@PreAuthorize("hasRole('ADMIN')")` for role-based access
- Extracting user info from JWT claims (`sub`, `preferred_username`, `realm_access`)
- CORS configuration (Angular on :4200 calling API on :8080)
- Swagger UI OAuth2 integration (login via Keycloak to test protected endpoints)
- Realm export/import for reproducible Keycloak setup

### Checklist

- [ ] Docker Compose with Keycloak running on port 8180
- [ ] Realm "task-manager" created with roles and test users
- [ ] Realm export saved for reproducibility
- [ ] Spring Security validates Keycloak JWTs
- [ ] Unauthenticated requests get 401
- [ ] ROLE_USER can CRUD own tasks only
- [ ] ROLE_ADMIN can CRUD all tasks
- [ ] Swagger UI has "Authorize" button with Keycloak OAuth2 flow
- [ ] CORS configured for localhost:4200
- [ ] Task.userId populated from JWT on creation
- [ ] Tests updated (mock JWT for security tests)
- [ ] CI pipeline passes (Keycloak not needed for unit tests)

---

## Phase 9: Angular Frontend

**Branch:** `feature/09-angular-frontend`
**Goal:** Build the Angular SPA with Keycloak login and task management UI.

### Project Structure

```
frontend/                           # Angular project (separate from backend)
├── angular.json
├── package.json
├── src/
│   ├── app/
│   │   ├── app.component.ts        # Root component + layout
│   │   ├── app.routes.ts           # Route definitions
│   │   ├── core/
│   │   │   ├── auth/
│   │   │   │   ├── auth.service.ts          # Keycloak OIDC integration
│   │   │   │   ├── auth.guard.ts            # Route protection
│   │   │   │   └── auth.interceptor.ts      # Attach JWT to HTTP requests
│   │   │   └── api/
│   │   │       └── task.service.ts          # HTTP calls to Spring Boot API
│   │   ├── features/
│   │   │   ├── task-list/
│   │   │   │   └── task-list.component.ts   # List + filter + delete
│   │   │   ├── task-form/
│   │   │   │   └── task-form.component.ts   # Create + edit (reactive form)
│   │   │   ├── task-detail/
│   │   │   │   └── task-detail.component.ts # View single task
│   │   │   └── login/
│   │   │       └── login.component.ts       # Login button → Keycloak redirect
│   │   └── shared/
│   │       ├── models/task.model.ts         # TypeScript interfaces
│   │       └── components/                  # Reusable UI components
│   ├── environments/
│   │   ├── environment.ts                   # API URL, Keycloak config
│   │   └── environment.prod.ts
│   └── styles.css                           # Global styles (or Tailwind/Material)
```

### What You Learn

- Angular CLI: `ng new`, `ng generate`, `ng serve`, `ng build`
- Components, services, modules (standalone components)
- Reactive forms with validation
- Angular HTTP client + interceptors
- Angular Router (routes, guards, lazy loading)
- OIDC authentication with `angular-oauth2-oidc`
- PKCE flow (the secure way to do OAuth2 in SPAs)
- Environment configuration (dev vs prod)
- Proxy config (Angular dev server → Spring Boot API)
- TypeScript interfaces matching OpenAPI DTOs

### Checklist

- [ ] Angular project scaffolded in `frontend/` directory
- [ ] angular-oauth2-oidc configured with Keycloak
- [ ] Login/logout flow working (redirects to Keycloak)
- [ ] Auth guard protects task routes
- [ ] HTTP interceptor attaches JWT to API calls
- [ ] Task list page (displays all tasks)
- [ ] Task create/edit form (reactive form with validation)
- [ ] Task delete with confirmation
- [ ] Status/priority shown with visual indicators (colors/icons)
- [ ] Show logged-in user name + role
- [ ] Proxy config for dev (localhost:4200 → localhost:8080)
- [ ] `ng build` produces production bundle
- [ ] CI pipeline updated to build Angular too

---

## Phase 10: Validation + Error Handling + Polish

**Branch:** `feature/10-validation-polish`
**Goal:** Add proper validation, error handling, pagination, and search.

### API Enhancements

```
GET /api/tasks?status=TODO&priority=HIGH&page=0&size=10&sort=dueDate,asc
                │          │              │       │      │
                │          │              │       │      └── Sort field + direction
                │          │              │       └── Page size
                │          │              └── Page number (0-indexed)
                │          └── Filter by priority
                └── Filter by status
```

### What You Learn

- Bean validation: `@NotBlank`, `@Size`, `@Future`, `@Valid`
- Custom validators (if needed)
- `@ControllerAdvice` + RFC 7807 Problem Details (standard error format)
- Spring Data `Pageable` + `Page<T>` for pagination
- Spring Data `Specification<T>` for dynamic filtering
- Angular: paginated table, filter bar, validation error display
- OpenAPI: documenting query params, pagination, error schemas

### Checklist

- [ ] Request DTOs have validation annotations
- [ ] Invalid requests return 400 with field-level errors
- [ ] Error format follows RFC 7807 Problem Details
- [ ] Pagination working (page, size, sort parameters)
- [ ] Filter by status, priority, due date range
- [ ] Angular shows validation errors on forms
- [ ] Angular has paginated table with sort headers
- [ ] Angular has filter bar (dropdowns + date picker)
- [ ] OpenAPI spec documents all query params and error responses
- [ ] Tests cover validation and error scenarios

---

## Phase 11: Categories + Tags + Richer Model

**Branch:** `feature/11-categories-tags`
**Goal:** Add entity relationships — categories (one-to-many) and tags (many-to-many).

### Updated Data Model

```
Category (1) ──────> (*) Task (*) <──────> (*) Tag
                          │
                          └── userId (from JWT)

Category                    Tag
├── id: Long                ├── id: Long
├── name: String            └── name: String
├── color: String
└── userId: String
```

### What You Learn

- JPA relationships: `@ManyToOne`, `@ManyToMany`, `@JoinTable`
- Cascade types and fetch strategies (EAGER vs LAZY)
- Nested REST resources (`/api/categories/{id}/tasks`)
- Angular: dropdown selects, tag chips (add/remove)
- OpenAPI: nested schemas, `$ref` references
- Keycloak: "My categories" and "my tags" scoped to user

### Checklist

- [ ] Category entity + CRUD endpoints
- [ ] Tag entity + CRUD endpoints
- [ ] Task → Category (many-to-one) relationship
- [ ] Task ↔ Tag (many-to-many) relationship
- [ ] Angular: category dropdown on task form
- [ ] Angular: tag chips (select/create tags)
- [ ] Filter tasks by category and/or tag
- [ ] Data scoped to logged-in user
- [ ] OpenAPI spec reflects nested relationships
- [ ] Tests cover relationship scenarios

---

## Phase 12: Dashboard + Analytics

**Branch:** `feature/12-dashboard`
**Goal:** Add aggregation endpoints and visual dashboard with charts.

### API Endpoints

```
GET /api/dashboard/summary         Tasks by status (counts)
GET /api/dashboard/overdue         Overdue tasks count + list
GET /api/dashboard/by-priority     Tasks grouped by priority
GET /api/dashboard/weekly-trend    Completed tasks per week (last 8 weeks)
```

### What You Learn

- JPQL aggregate queries (`COUNT`, `GROUP BY`)
- Custom repository methods
- Angular charting library (ng2-charts / Chart.js)
- Dashboard layout design
- Keycloak admin API (list users — admin only)
- CI/CD updates: build + deploy Angular with Spring Boot

### Checklist

- [ ] Dashboard controller with aggregation endpoints
- [ ] JPQL queries for counts and grouping
- [ ] Angular dashboard page with charts
- [ ] Task completion chart (pie/donut)
- [ ] Priority breakdown chart (bar)
- [ ] Weekly trend chart (line)
- [ ] Overdue tasks alert panel
- [ ] Admin: user list (from Keycloak admin API)
- [ ] CI/CD builds Angular + packages with Spring Boot
- [ ] Deploy to CF with full-stack app

---

## Deployment Options (to decide later)

When we reach deployment phases, we'll need to decide how to handle Keycloak
in production:

```
Option A: Keycloak on Docker (CF or separate VM)
  + Full control
  - Need to manage Keycloak infrastructure

Option B: SAP IAS (Identity Authentication Service)
  + Native to SAP BTP
  + No extra infrastructure
  - Different config than Keycloak (but same OAuth2/OIDC protocol)

Option C: Auth0 / Okta free tier
  + Managed service, easy setup
  - External dependency
```

We'll decide when we get there. The app's use of standard OAuth2/OIDC means
switching identity providers is mostly a configuration change.
