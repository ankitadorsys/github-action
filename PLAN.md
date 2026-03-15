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
| Backend | Spring Boot 4.0.3 | REST API, JPA, validation |
| Frontend | Angular 17+ | SPA, reactive forms, routing |
| API Docs | OpenAPI 3.0 (springdoc 3.0.2) | Auto-generated spec + Swagger UI |
| API Generation | openapi-generator-maven-plugin 7.20.0 | Contract-first: generates interfaces + DTOs |
| Entity Mapping | MapStruct 1.6.3 | Compile-time entity-to-DTO mapping |
| Entity Boilerplate | Lombok | Compile-time getter/setter/builder generation |
| Auth | Keycloak (Docker) | OAuth2/OIDC identity provider |
| Auth Protocol | OAuth2 + OIDC + PKCE | Secure token-based authentication |
| Backend Security | spring-boot-starter-oauth2-resource-server | JWT validation |
| Frontend Auth | angular-oauth2-oidc | OIDC login flow with PKCE |
| Database | H2 → PostgreSQL | Start simple, upgrade later |
| Containerization | Docker Compose | Run Keycloak + DB locally |

---

## Phase 7: Task CRUD API + OpenAPI — DONE

**Branch:** `feature/07-task-crud-api`
**Goal:** Build full CRUD API with entity relationships, API-first approach,
OpenAPI code generation, MapStruct mapping, and comprehensive tests.

### Approach: API-First (Contract-First)

1. Define API contract in `api/api.yaml` (single source of truth)
2. `openapi-generator-maven-plugin` generates Java interfaces + DTO classes
3. Controllers implement the generated interfaces
4. MapStruct mappers convert between JPA entities and generated DTOs

### Data Model (with Relationships)

```
Category (1) ──── (*) Task (*) ──── (*) Tag
                       │
                       │ (1)
                       │
                      (*) TaskComment

  Category  1 ──── * Task       (@OneToMany / @ManyToOne)
  Task      * ──── * Tag        (@ManyToMany via join table task_tags)
  Task      1 ──── * Comment    (@OneToMany with cascade + orphanRemoval)
```

### API Endpoints

```
GET    /api/hello                          Hello endpoint (existing)
GET    /api/tasks                          List all tasks
POST   /api/tasks                          Create a new task
GET    /api/tasks/{id}                     Get task by ID
PUT    /api/tasks/{id}                     Update a task
DELETE /api/tasks/{id}                     Delete a task
GET    /api/categories                     List all categories
POST   /api/categories                     Create a category
GET    /api/categories/{id}                Get category by ID
PUT    /api/categories/{id}                Update a category
DELETE /api/categories/{id}                Delete a category
GET    /api/tags                           List all tags
POST   /api/tags                           Create a tag
GET    /api/tags/{id}                      Get tag by ID
PUT    /api/tags/{id}                      Update a tag
DELETE /api/tags/{id}                      Delete a tag
GET    /api/tasks/{taskId}/comments        List comments for a task
POST   /api/tasks/{taskId}/comments        Add a comment to a task
DELETE /api/tasks/{taskId}/comments/{id}   Delete a comment
```

### Files Created/Modified

**Modified:**
- `backend/pom.xml` — Boot 4.0.3, Java 21, springdoc 3.0.2, Lombok, MapStruct, openapi-generator, test deps
- `.github/workflows/pipeline.yml` — matrix [21, 25]
- `.github/workflows/reusable-build.yml` — setup-java@v5
- `src/main/resources/application.yml` — H2 + JPA + OpenAPI + Actuator config
- `src/test/.../controller/HelloControllerTest.java` — added @DisplayName

**Created (source):**
- `api/api.yaml` — OpenAPI 3.0.3 spec
- `src/.../controller/TaskController.java` — implements TasksApi
- `src/.../controller/CategoryController.java` — implements CategoriesApi
- `src/.../controller/TagController.java` — implements TagsApi
- `src/.../controller/CommentController.java` — implements CommentsApi
- `src/.../model/Task.java` — JPA entity with all relationships, Lombok
- `src/.../model/Category.java` — @OneToMany(mappedBy="category")
- `src/.../model/Tag.java` — @ManyToMany(mappedBy="tags")
- `src/.../model/TaskComment.java` — @ManyToOne(task)
- `src/.../repository/TaskRepository.java`
- `src/.../repository/CategoryRepository.java`
- `src/.../repository/TagRepository.java`
- `src/.../repository/TaskCommentRepository.java`
- `src/.../service/TaskService.java`
- `src/.../service/CategoryService.java`
- `src/.../service/TagService.java`
- `src/.../service/CommentService.java`
- `src/.../mapper/DateTimeMapper.java` — shared LocalDateTime→OffsetDateTime
- `src/.../mapper/TaskMapper.java`
- `src/.../mapper/CategoryMapper.java`
- `src/.../mapper/TagMapper.java`
- `src/.../mapper/CommentMapper.java`
- `src/.../exception/TaskNotFoundException.java`
- `src/.../exception/ResourceNotFoundException.java`
- `src/.../exception/GlobalExceptionHandler.java`

**Created (tests):**
- `src/test/.../SpringIntegrationTest.java` — 29 end-to-end tests
- `src/test/.../controller/TaskControllerTest.java` — 9 unit tests
- `src/test/.../controller/CategoryControllerTest.java` — 7 unit tests
- `src/test/.../controller/TagControllerTest.java` — 7 unit tests
- `src/test/.../controller/CommentControllerTest.java` — 6 unit tests
- `src/test/.../service/TaskServiceTest.java` — 10 unit tests
- `src/test/.../service/CategoryServiceTest.java` — 7 unit tests
- `src/test/.../service/TagServiceTest.java` — 7 unit tests
- `src/test/.../service/CommentServiceTest.java` — 6 unit tests

### Test Summary: 90 tests, all passing

| Test Class | Count | Type |
|-----------|-------|------|
| SpringIntegrationTest | 29 | Integration (full stack, H2, TestRestTemplate) |
| TaskControllerTest | 9 | Unit (@WebMvcTest + MockMvc + @MockitoBean) |
| CategoryControllerTest | 7 | Unit (@WebMvcTest + MockMvc) |
| TagControllerTest | 7 | Unit (@WebMvcTest + MockMvc) |
| CommentControllerTest | 6 | Unit (@WebMvcTest + MockMvc) |
| TaskServiceTest | 10 | Unit (@ExtendWith(MockitoExtension) + @Mock) |
| CategoryServiceTest | 7 | Unit (Mockito) |
| TagServiceTest | 7 | Unit (Mockito) |
| CommentServiceTest | 6 | Unit (Mockito) |
| HelloControllerTest | 2 | Unit (@WebMvcTest + MockMvc) |

### What Was Learned

- API-First / Contract-First approach with openapi-generator
- JPA entities with all relationship types: @OneToMany, @ManyToOne, @ManyToMany
- Cascade types and orphanRemoval
- MapStruct for compile-time entity-to-DTO mapping
- Lombok for entity boilerplate elimination
- Spring Boot 4.0 breaking changes (5 different issues found and fixed)
- Jackson 3 as primary ObjectMapper in Boot 4.0
- H2 in-memory database with JPA auto-DDL
- Global exception handling with @ControllerAdvice
- springdoc-openapi Swagger UI integration

### Checklist

- [x] Spring Boot upgraded to 4.0.3, Java 21
- [x] H2 + JPA + springdoc + Lombok + MapStruct dependencies added
- [x] OpenAPI YAML spec created (single source of truth)
- [x] openapi-generator-maven-plugin configured with useSpringBoot4=true
- [x] All JPA entities with relationships created (Task, Category, Tag, TaskComment)
- [x] All repositories created
- [x] All services with CRUD operations created
- [x] All controllers implementing generated API interfaces created
- [x] MapStruct mappers created (4 mappers + shared DateTimeMapper)
- [x] Global exception handler returns proper error responses
- [x] Swagger UI accessible at /swagger-ui.html
- [x] OpenAPI spec at /v3/api-docs shows all endpoints
- [x] H2 console accessible at /h2-console
- [x] 90 tests — all passing (unit + integration)
- [x] Existing /api/hello endpoint still works
- [x] CI pipeline updated (matrix [21,25], setup-java@v5)

---

## Phase 8: Keycloak + Spring Security — DONE

**Branch:** `feature/08-keycloak-security`
**Goal:** Set up Keycloak via Docker and secure the API with OAuth2/JWT.

### Keycloak Configuration

```
Keycloak 26.2.5 (Docker)
└── Realm: task-manager
    ├── Client: task-manager-api (backend — confidential)
    │   └── Service account: validates tokens
    ├── Client: task-manager-web (frontend — public, PKCE)
    │   └── Redirect URI: http://localhost:4200/*
    ├── Realm Roles:
    │   ├── ROLE_ADMIN — can manage all tasks + categories + tags
    │   └── ROLE_USER — can manage own tasks only, read categories/tags
    └── Test Users:
        ├── admin / admin123 (ROLE_ADMIN + ROLE_USER)
        └── user1 / user123 (ROLE_USER)
```

### Security Architecture

```
Controller layer:    @PreAuthorize for role checks, calls AuthenticationService.getCurrentUser()
     ↓ passes userId/isAdmin as params to service methods
Service layer:       Business logic, receives userId as parameter (no security awareness)
AuthenticationService: Extracts user info from JWT SecurityContext (falls back to anonymous admin when no JWT)
SecurityConfig:      JWT validation, Keycloak realm_access role mapping, @EnableMethodSecurity (@Profile !nosecurity)
NoSecurityConfig:    Permits all (@Profile nosecurity) — for integration tests without Keycloak
TestSecurityConfig:  Same SecurityFilterChain but NO @EnableMethodSecurity — for @WebMvcTest
```

### Authorization Model

| Resource | ROLE_USER | ROLE_ADMIN |
|----------|-----------|------------|
| Tasks | CRUD own tasks only | CRUD all tasks |
| Categories | Read only | Full CRUD |
| Tags | Read only | Full CRUD |
| Comments | Full CRUD | Full CRUD |
| Public endpoints | Open | Open |

Public: `/api/hello`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`, `/h2-console/**`

### Files Created/Modified

**Modified:**
- `backend/pom.xml` — added oauth2-resource-server + spring-security-test deps
- `src/.../controller/TaskController.java` — @PreAuthorize + authenticationService.getCurrentUser()
- `src/.../controller/CategoryController.java` — @PreAuthorize role checks
- `src/.../controller/TagController.java` — @PreAuthorize role checks
- `src/.../controller/CommentController.java` — @PreAuthorize role checks
- `src/.../service/TaskService.java` — userId/isAdmin params, verifyOwnership(), findByUserId()
- `src/.../model/Task.java` — added userId field
- `src/.../repository/TaskRepository.java` — added findByUserId()
- `src/.../exception/GlobalExceptionHandler.java` — AccessDeniedException → 403
- `src/main/resources/application.yml` — JWT issuer-uri config
- `api/api.yaml` — bearerAuth, 401/403 responses, userId in TaskResponse
- All controller tests — @Import(TestSecurityConfig), security mocks
- `src/test/.../service/TaskServiceTest.java` — userId/isAdmin param tests
- `src/test/.../SpringIntegrationTest.java` — @ActiveProfiles("nosecurity")

**Created:**
- `backend/docker-compose.yml` — Keycloak 26.2.5 on port 8180
- `backend/keycloak/realm-export.json` — Pre-configured realm
- `src/.../config/SecurityConfig.java` — JWT + @EnableMethodSecurity + Keycloak role converter
- `src/.../config/NoSecurityConfig.java` — @Profile("nosecurity") permits all
- `src/.../security/AuthenticatedUser.java` — Record(userId, username, roles)
- `src/.../security/AuthenticationService.java` — Extract user from JWT, fallback to anonymous
- `src/main/resources/application-nosecurity.yml` — blanks out issuer-uri
- `src/test/.../config/TestSecurityConfig.java` — no @EnableMethodSecurity for @WebMvcTest
- `src/test/.../controller/SecurityTest.java` — 18 auth/authz tests (@SpringBootTest)

### Test Summary: 114 tests, all passing

| Test Class | Count | Type |
|-----------|-------|------|
| SpringIntegrationTest | 29 | Integration (nosecurity profile, TestRestTemplate, H2) |
| TaskControllerTest | 9 | Unit (@WebMvcTest + TestSecurityConfig + MockMvc) |
| CategoryControllerTest | 7 | Unit (@WebMvcTest + TestSecurityConfig) |
| TagControllerTest | 7 | Unit (@WebMvcTest + TestSecurityConfig) |
| CommentControllerTest | 6 | Unit (@WebMvcTest + TestSecurityConfig) |
| HelloControllerTest | 2 | Unit (@WebMvcTest + TestSecurityConfig) |
| SecurityTest | 18 | Security (@SpringBootTest + MockJwtDecoder + @PreAuthorize) |
| TaskServiceTest | 16 | Unit (Mockito — userId/isAdmin/ownership tests) |
| CategoryServiceTest | 7 | Unit (Mockito) |
| TagServiceTest | 7 | Unit (Mockito) |
| CommentServiceTest | 6 | Unit (Mockito) |

### Key Discoveries

1. **@WebMvcTest + @EnableMethodSecurity proxy issue**: @PreAuthorize creates proxies on controllers implementing generated interfaces, breaking MVC handler resolution → 404. Fix: TestSecurityConfig without @EnableMethodSecurity for unit tests.
2. **SecurityTest as @SpringBootTest**: Full context needed for @PreAuthorize testing. @MockitoBean JwtDecoder prevents Keycloak startup lookup.
3. **AuthenticationService fallback**: Returns anonymous admin user when no JWT present — allows nosecurity profile integration tests to work without mocking security context.

### What Was Learned

- OAuth2 / OpenID Connect protocol (authorization code flow + PKCE)
- Keycloak concepts: realms, clients, roles, users
- Docker Compose for local development infrastructure
- Spring Security as OAuth2 Resource Server (validates JWTs)
- JWT anatomy: header, payload (claims), signature
- `@PreAuthorize("hasRole('ADMIN')")` for role-based access
- Extracting user info from JWT claims (`sub`, `preferred_username`, `realm_access`)
- CORS configuration (Angular on :4200 calling API on :8080)
- Realm export/import for reproducible Keycloak setup
- @EnableMethodSecurity proxy interaction with @WebMvcTest
- Profile-based security configs (nosecurity for tests, real security for production)

### Checklist

- [x] Docker Compose with Keycloak 26.2.5 running on port 8180
- [x] Realm "task-manager" created with roles and test users
- [x] Realm export saved for reproducibility
- [x] Spring Security validates Keycloak JWTs
- [x] Unauthenticated requests get 401
- [x] ROLE_USER can CRUD own tasks only
- [x] ROLE_ADMIN can CRUD all tasks
- [x] CORS configured for localhost:4200
- [x] Task.userId populated from JWT on creation
- [x] Tests updated (mock JWT for security tests)
- [x] 114 tests — all passing
- [x] CI pipeline passes (Keycloak not needed for unit tests)
- [ ] Swagger UI has "Authorize" button with Keycloak OAuth2 flow (deferred to Phase 9)

---

## Phase 9: Angular Frontend

**Branch:** `feature/09-angular-frontend`
**Goal:** Build the Angular SPA with Keycloak login and task management UI.

**Status:** DONE

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

- [x] Angular project scaffolded in `frontend/` directory
- [x] angular-oauth2-oidc configured with Keycloak (base auth service + interceptor)
- [x] Login/logout flow working (redirects to Keycloak)
- [x] Auth guard protects task routes
- [x] HTTP interceptor attaches JWT to API calls
- [x] Task list page (displays all tasks)
- [x] Task create/edit form (reactive form with validation)
- [x] Task delete with confirmation
- [x] Status/priority shown with visual indicators (colors/icons)
- [x] Show logged-in user name + role
- [x] Proxy config for dev (localhost:4200 → localhost:8080)
- [x] `ng build` produces production bundle
- [x] CI pipeline updated to build Angular too

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

## Phase 11: Dashboard + Analytics (was Categories + Tags — moved to Phase 7)

**Branch:** `feature/11-dashboard`
**Goal:** Add aggregation endpoints and visual dashboard with charts.

> Note: Categories, Tags, and entity relationships were originally planned for Phase 11
> but were pulled into Phase 7 to build a complete data model from the start.

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

### Checklist

- [ ] Dashboard controller with aggregation endpoints
- [ ] JPQL queries for counts and grouping
- [ ] Angular dashboard page with charts
- [ ] Task completion chart (pie/donut)
- [ ] Priority breakdown chart (bar)
- [ ] Weekly trend chart (line)
- [ ] Overdue tasks alert panel
- [ ] Tests cover aggregation queries

---

## Phase 12: Production Polish + Full Deployment

**Branch:** `feature/12-production-deploy`
**Goal:** CI/CD builds Angular + packages with Spring Boot, deploy full stack to CF.

### Checklist

- [ ] CI/CD builds Angular + packages with Spring Boot
- [ ] Deploy to CF with full-stack app
- [ ] Admin: user list (from Keycloak admin API)
- [ ] Production configuration profiles

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
