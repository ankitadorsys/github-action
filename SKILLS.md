# SKILLS.md — Project Context (Read This First)

> **Purpose:** This file is the persistent context for AI assistants working on this repo.
> Scan this file at the start of every session to recall project state, conventions, and decisions.

## Project Overview

A learning project to master **GitHub Actions** by building a Spring Boot application
and deploying it to **Cloud Foundry** (SAP BTP) using **Terraform** for infrastructure.

**Repository:** https://github.com/ankitadorsys/github-action

## Tech Stack

| Component         | Choice                                      |
|-------------------|---------------------------------------------|
| Language          | Java 21                                     |
| Framework         | Spring Boot 3.x                             |
| Build Tool        | Maven                                       |
| CI/CD             | GitHub Actions                              |
| IaC               | Terraform (Cloud Foundry provider)          |
| Deployment Target | Cloud Foundry on SAP BTP (free trial tier)  |

## SAP BTP Cloud Foundry Details

| Property        | Value                     |
|-----------------|---------------------------|
| Region          | Singapore - Azure         |
| Global Account  | 86d1d2ddtrial             |
| Subaccount      | trial                     |
| CF Org          | 86d1d2ddtrial             |
| CF Space        | dev                       |
| User ID         | ***REDACTED_ID***               |
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
| Java version                  | 21                | Latest LTS                                        |
| Build tool                    | Maven             | Most common for Spring Boot, simpler for learning |
| Deployment approach           | Phased (6 phases) | Incremental learning, simple to advanced          |
| Context management            | SKILLS.md + PLAN.md | Persistent context across AI sessions           |

## Prerequisites & External Setup

### Accounts
- [x] SAP BTP Trial account — signed up, region: Singapore - Azure
- [x] Cloud Foundry environment enabled in SAP BTP subaccount
- [x] Confirm CF API endpoint: https://api.cf.ap21.hana.ondemand.com

### Local Tools
- [ ] Java 21 (`brew install openjdk@21`)
- [ ] Maven (`brew install maven`)
- [ ] Cloud Foundry CLI v8 (`brew install cloudfoundry/tap/cf-cli@8`)
- [ ] Terraform (`brew install terraform`)

### GitHub Repository Secrets (configure before Phase 3)
- `CF_API_ENDPOINT` — SAP BTP CF API endpoint
- `CF_USERNAME` — SAP BTP email/user ID
- `CF_PASSWORD` — SAP BTP password
- `CF_ORG` — `86d1d2ddtrial`
- `CF_SPACE` — `dev`

## Current Status

- **Active Phase:** Phase 3 — Manual CF Deploy Workflow
- **Active Branch:** `feature/03-manual-cf-deploy`
- **Completed Phases:** Phase 1 (Spring Boot App), Phase 2 (Basic CI Workflow)
- **Blockers:** GitHub Secrets need to be configured before testing deploy
- **Last updated:** 2026-03-14
