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
| User ID         | P2011999649               |
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
- [x] Cloud Foundry CLI v8 (installed — `cf version 8.18.0`)
- [ ] Java 21 (`brew install openjdk@21`)
- [ ] Maven (`brew install maven`)
- [ ] Terraform (`brew install terraform`)

### GitHub Repository Secrets (configured)
- `CF_API_ENDPOINT` — `https://api.cf.ap21.hana.ondemand.com`
- `CF_USERNAME` — email address (`ankitkrsingh2012@gmail.com`, NOT the P-number)
- `CF_PASSWORD` — SAP BTP password
- `CF_ORG` — `86d1d2ddtrial`
- `CF_SPACE` — `dev`

### CF Authentication Notes
- **IDP Origin:** `sap.ids` (SAP ID Service — the default identity provider)
- **Username:** Must be the email address, not the SAP user ID (P2011999649)
- **Auth command:** `cf auth "<email>" "<password>" --origin sap.ids`
- **App URL:** https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com

## Current Status

- **Active Phase:** Phase 4 complete — ready for Phase 5
- **Active Branch:** `main`
- **Completed Phases:** Phase 1 (Spring Boot App), Phase 2 (Basic CI), Phase 3 (Manual CF Deploy), Phase 4 (Terraform CF Infra)
- **Deployed App:** https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com/api/hello
- **Terraform:** App + route managed by Terraform (state is local, gitignored)
- **Blockers:** None
- **Next:** Phase 5 — Full CI/CD Pipeline (Terraform plan on PR, apply on merge)
- **Last updated:** 2026-03-14
