# SKILLS.md — Project Context (Read This First)

> **Purpose:** This file is the persistent context for AI assistants working on this repo.
> Scan this file at the start of every session to recall project state, conventions, and decisions.

## Project Overview

A learning project that started with mastering **GitHub Actions** (CI/CD pipeline
with Terraform + Cloud Foundry) and is now extending into a **full-stack Task Manager**
with Spring Boot, Angular, OpenAPI, and Keycloak authentication.

**Repository:** https://github.com/ankitadorsys/github-action

## Tech Stack

| Component         | Choice                                      |
|-------------------|---------------------------------------------|
| Language          | Java 21 (compile + runtime + CF)            |
| CI Matrix         | Java [21, 25] (21 for prod, 25 for forward-compat) |
| Framework         | Spring Boot 4.0.3                           |
| Frontend          | Angular 17+ (standalone components) — Phase 9 |
| API Docs          | OpenAPI 3.0 (springdoc-openapi 3.0.2)      |
| API Approach      | Contract-First (openapi-generator-maven-plugin 7.20.0) |
| Auth/IdP          | Keycloak (Docker) via OAuth2/OIDC — Phase 8 |
| Build Tool        | Maven (backend), Angular CLI (frontend)     |
| CI/CD             | GitHub Actions (setup-java@v5)              |
| IaC               | Terraform (Cloud Foundry provider)          |
| Database          | H2 (dev) → PostgreSQL (later)              |
| Deployment Target | Cloud Foundry on SAP BTP (free trial tier)  |
| Lombok            | Managed by Boot parent (compile-time only)  |
| MapStruct         | 1.6.3 (compile-time mapper generation)      |

## SAP BTP Cloud Foundry Details

| Property        | Value                     |
|-----------------|---------------------------|
| Region          | Singapore - Azure         |
| Global Account  | 86d1d2ddtrial             |
| Subaccount      | trial                     |
| CF Org          | 86d1d2ddtrial             |
| CF Space        | dev                       |
| User ID         | (redacted)                |
| CF API Endpoint | https://api.cf.ap21.hana.ondemand.com |

## Conventions

- **Branch naming:** `feature/<phase-number>-<short-description>` (e.g., `feature/01-spring-boot-app`)
- **Do NOT delete branches** after merge
- **Workflow:** Create branch -> write code -> push -> merge directly to `main` (PAT cannot create/merge PRs)
- **Commit style:** Conventional — `feat:`, `fix:`, `chore:`, `docs:`, `ci:`
- **Comments:** Minimal — code should be self-explanatory. Only add brief comments where something is genuinely non-obvious. Explain concepts in conversation instead.
- **Tests:** Always use `@DisplayName` on all test classes and test methods
- **No branch protection rules** on the repo
- **Project structure:**
  ```
  github-action/
  ├── src/main/java/com/example/demo/    # Java source
  │   ├── controller/                     # REST controllers (implement generated API interfaces)
  │   ├── model/                          # JPA entities (use Lombok)
  │   ├── repository/                     # Spring Data JPA repositories
  │   ├── service/                        # Business logic layer
  │   ├── mapper/                         # MapStruct mapper interfaces
  │   └── exception/                      # Custom exceptions + GlobalExceptionHandler
  ├── src/main/resources/
  │   ├── application.yml                 # H2, JPA, OpenAPI, Actuator config
  │   └── openapi/api.yaml               # OpenAPI spec (single source of truth)
  ├── src/test/java/com/example/demo/    # Tests
  ├── target/generated-sources/openapi/  # Auto-generated API interfaces + DTOs
  ├── terraform/                          # Terraform configs (Phase 4+)
  ├── .github/workflows/                  # GitHub Actions (Phase 2+)
  ├── manifest.yml                        # CF deployment manifest
  ├── pom.xml                             # Maven build
  ├── SKILLS.md                           # This file
  └── PLAN.md                             # Phased roadmap
  ```

## Key Decisions Log

| Decision                      | Choice            | Reason                                           |
|-------------------------------|-------------------|--------------------------------------------------|
| CF provider                   | SAP BTP free tier | Free, real CF environment, good Terraform support |
| Java version                  | 21 everywhere     | SAP BTP CF buildpack max is JRE 21               |
| CI matrix                     | [21, 25]          | 21 for production compat, 25 for forward-compat  |
| Build tool                    | Maven             | Most common for Spring Boot, simpler for learning |
| Deployment approach           | Phased            | Incremental learning, simple to advanced          |
| Context management            | SKILLS.md + PLAN.md | Persistent context across AI sessions           |
| Terraform state in CI         | Dedicated `terraform-state` branch | Simple, no extra services, good for learning |
| Identity provider             | Keycloak (Docker) | Industry standard, open-source, great Spring integration |
| Auth protocol                 | OAuth2/OIDC + PKCE | Standard, secure SPA flow via Keycloak          |
| Frontend framework            | Angular           | Enterprise standard, good with Spring Boot       |
| API approach                  | Contract-First (API-First) | OpenAPI YAML is single source of truth; openapi-generator generates interfaces + DTOs |
| DTO generation                | openapi-generator-maven-plugin | Auto-generates from api.yaml — no hand-written DTOs |
| Entity boilerplate            | Lombok            | @Getter/@Setter/@Builder etc. — compile-time only |
| Entity-to-DTO mapping         | MapStruct 1.6.3   | Compile-time mapper generation, zero runtime overhead |
| Entity relationships          | All types in Phase 7 | @OneToMany, @ManyToOne, @ManyToMany pulled from Phase 11 |
| HelloController               | Does NOT implement HelloApi | Generated HelloApi returns ResponseEntity<String> text/plain, but existing controller returns Map JSON. Changing would break tests. |
| Monorepo restructure          | Phase 9           | backend/ + frontend/ + api-spec/ split happens when Angular arrives |

## Version Decisions

| Tool | Version | Notes |
|------|---------|-------|
| Spring Boot | 4.0.3 | Spring Framework 7.0, Hibernate 7.1, Jakarta EE 11 |
| Java | 21 | Compile + runtime + CF. JDK 25 in CI matrix only. |
| springdoc-openapi | 3.0.2 | v3.x for Boot 4.x |
| setup-java action | v5 | Supports JDK 25 Temurin |
| openapi-generator-maven-plugin | 7.20.0 | v7.20.0 added `useSpringBoot4=true` |
| swagger-annotations-jakarta | 2.2.30 | Required by generated code |
| MapStruct | 1.6.3 | With lombok-mapstruct-binding 0.2.0 |
| Lombok | Managed by Boot parent | scope: provided |

## Spring Boot 4.0 Breaking Changes (Found & Fixed)

1. **`@WebMvcTest` package move** — Now in `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`. Requires `spring-boot-webmvc-test` test dependency.
2. **`@MockBean` → `@MockitoBean`** — Deprecated `@MockBean` replaced by `@MockitoBean` from `org.springframework.test.context.bean.override.mockito`.
3. **`TestRestTemplate` package move** — Now in `org.springframework.boot.resttestclient.TestRestTemplate`. Requires `spring-boot-resttestclient` test dependency + `@AutoConfigureTestRestTemplate` annotation.
4. **`RestTemplateBuilder` dependency** — `spring-boot-resttestclient` needs `spring-boot-restclient` on the classpath (provides `RestTemplateBuilder`). Without it: `NoClassDefFoundError`.
5. **Jackson 3 is primary** — Auto-configured `ObjectMapper` is `tools.jackson.databind.ObjectMapper` (Jackson 3), NOT `com.fasterxml.jackson.databind.ObjectMapper` (Jackson 2). All test files must use Jackson 3 imports. Jackson 2 annotations still work.

## API-First Architecture (Phase 7)

### How It Works
1. Define API contract in `src/main/resources/openapi/api.yaml`
2. `openapi-generator-maven-plugin` generates Java interfaces + DTO classes into `target/generated-sources/openapi/`
3. Controllers `implement` the generated interfaces (e.g., `TaskController implements TasksApi`)
4. MapStruct mappers convert between JPA entities and generated DTOs

### OpenAPI Generator Config
```xml
<configOptions>
    <useSpringBoot4>true</useSpringBoot4>
    <interfaceOnly>true</interfaceOnly>
    <useTags>true</useTags>
    <openApiNullable>false</openApiNullable>
    <documentationProvider>springdoc</documentationProvider>
    <useBeanValidation>true</useBeanValidation>
    <dateLibrary>java8</dateLibrary>
    <useResponseEntity>true</useResponseEntity>
</configOptions>
```

### Generated Code Structure
- **API Interfaces**: `TasksApi`, `CategoriesApi`, `TagsApi`, `CommentsApi`, `HelloApi` in `com.example.demo.api`
- **DTO Classes**: `TaskRequest`, `TaskResponse`, `CategoryRequest`, `CategoryResponse`, `TagRequest`, `TagResponse`, `CommentRequest`, `CommentResponse`, `ErrorResponse`, `ValidationError` in `com.example.demo.api.model`
- **Enums**: `TaskStatus`, `TaskPriority` in `com.example.demo.api.model`
- Generated DTOs use fluent builder pattern: `new TaskResponse().id(1L).title("foo")`
- Generated interfaces have default methods returning `HttpStatus.NOT_IMPLEMENTED` — controllers override these

### Entity Relationship Model
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

### MapStruct Configuration
- `maven-compiler-plugin` annotationProcessorPaths: mapstruct-processor, lombok, lombok-mapstruct-binding
- Global compiler arg: `-Amapstruct.defaultComponentModel=spring` (all mappers become Spring `@Component` beans)
- Shared `DateTimeMapper` for LocalDateTime→OffsetDateTime conversion
- Four mapper interfaces: `TaskMapper`, `CategoryMapper`, `TagMapper`, `CommentMapper`

## Prerequisites & External Setup

### Accounts
- [x] SAP BTP Trial account — signed up, region: Singapore - Azure
- [x] Cloud Foundry environment enabled in SAP BTP subaccount
- [x] Confirm CF API endpoint: https://api.cf.ap21.hana.ondemand.com

### Local Tools
- [x] Cloud Foundry CLI v8 (installed — `cf version 8.18.0`)
- [x] Java 21
- [x] Maven
- [x] Terraform

### GitHub Setup
- `gh` CLI installed and authenticated as `ankitkrsingh2012` with a classic PAT (`repo` + `workflow` scopes)
- PAT cannot create/merge PRs — merge directly to main
- No branch protection rules
- Git remote uses SSH (`git@github.com:ankitadorsys/github-action.git`)
- SSH key `~/.ssh/id_ed25519_github` configured for `github.com`

### GitHub Repository Secrets (configured)
- `CF_API_ENDPOINT` — `https://api.cf.ap21.hana.ondemand.com`
- `CF_USERNAME` — email address (NOT the P-number)
- `CF_PASSWORD` — SAP BTP password
- `CF_ORG` — `86d1d2ddtrial`
- `CF_SPACE` — `dev`

### CF Authentication Notes
- **IDP Origin:** `sap.ids` (SAP ID Service — the default identity provider)
- **Username:** Must be the email address, not the SAP user ID
- **Auth command:** `cf auth "<email>" "<password>" --origin sap.ids`
- **App URL:** https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com

## Current Status

- **Active Phase:** Phase 7 — COMPLETE
- **Active Branch:** `feature/07-task-crud-api` (ready to merge to `main`)
- **Part A (Phases 1-6):** COMPLETE — CI/CD pipeline with Terraform + CF deployed
- **Part B (Phases 7-12):** IN PROGRESS
- **Deployed App:** https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com/api/hello
- **Terraform:** App + route managed by Terraform; state on `terraform-state` branch
- **CI/CD Pipeline:** `.github/workflows/pipeline.yml` — matrix build (Java 21+25), reusable workflows, terraform plan on PR, terraform apply + deploy on merge to main
- **Tests:** 90 total (all passing) — 29 integration + 31 controller unit + 30 service unit
- **Next:** Phase 8 — Keycloak + Spring Security
- **Last updated:** 2026-03-14
