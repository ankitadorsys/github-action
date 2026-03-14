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
| Language          | Java 17 (compile target) / 21 (runtime)     |
| Framework         | Spring Boot 3.x                             |
| Frontend          | Angular 17+ (standalone components)         |
| API Docs          | OpenAPI 3.0 (springdoc-openapi)             |
| Auth/IdP          | Keycloak (Docker) via OAuth2/OIDC           |
| Build Tool        | Maven (backend), Angular CLI (frontend)     |
| CI/CD             | GitHub Actions                              |
| IaC               | Terraform (Cloud Foundry provider)          |
| Database          | H2 (dev) → PostgreSQL (later)              |
| Deployment Target | Cloud Foundry on SAP BTP (free trial tier)  |

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
- **Workflow:** Create branch -> write code -> push -> create PR -> merge to `main`
- **Commit style:** Conventional — `feat:`, `fix:`, `chore:`, `docs:`, `ci:`
- **Project structure:**
  ```
  github-action/
  ├── src/main/java/com/example/demo/    # Java source
  ├── src/main/resources/                 # Config files
  ├── src/test/java/com/example/demo/    # Tests
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
| Java version                  | 17 (compile) / 21 (runtime) | 17 for matrix compat, 21 JRE in CF |
| Build tool                    | Maven             | Most common for Spring Boot, simpler for learning |
| Deployment approach           | Phased            | Incremental learning, simple to advanced          |
| Context management            | SKILLS.md + PLAN.md | Persistent context across AI sessions           |
| Terraform state in CI         | Dedicated `terraform-state` branch | Simple, no extra services, good for learning |
| Identity provider             | Keycloak (Docker) | Industry standard, open-source, great Spring integration |
| Auth protocol                 | OAuth2/OIDC + PKCE | Standard, secure SPA flow via Keycloak          |
| Frontend framework            | Angular           | Enterprise standard, good with Spring Boot       |
| API documentation             | springdoc-openapi | Auto-generates OpenAPI 3.0 spec from code        |
| Frontend auth library         | angular-oauth2-oidc | Well-maintained, supports PKCE                 |
| App architecture              | API first, then auth, then UI | Secure from the start, no retrofit |

## Prerequisites & External Setup

### Accounts
- [x] SAP BTP Trial account — signed up, region: Singapore - Azure
- [x] Cloud Foundry environment enabled in SAP BTP subaccount
- [x] Confirm CF API endpoint: https://api.cf.ap21.hana.ondemand.com

### Local Tools
- [x] Cloud Foundry CLI v8 (installed — `cf version 8.18.0`)
- [ ] Java 21 (`brew install openjdk@21`)
- [ ] Maven (`brew install maven`)
- [ ] Terraform (`brew install terraform`)

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

- **Active Phase:** Phase 7 — Task CRUD API + OpenAPI (not yet started)
- **Active Branch:** `main` (will create `feature/07-task-crud-api`)
- **Part A (Phases 1-6):** COMPLETE — CI/CD pipeline with Terraform + CF deployed
- **Part B (Phases 7-12):** IN PROGRESS — Full-stack Task Manager
- **Deployed App:** https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com/api/hello
- **Terraform:** App + route managed by Terraform; state on `terraform-state` branch (updated by CI/CD pipeline)
- **CI/CD Pipeline:** `.github/workflows/pipeline.yml` — matrix build (Java 17+21), reusable workflows, terraform plan on PR, terraform apply + deploy on merge to main
- **Blockers:** None
- **Next:** Phase 7 — Add Task entity, CRUD API, springdoc-openapi, H2 database
- **Last updated:** 2026-03-14
