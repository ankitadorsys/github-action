# github-action-demo

[![CI/CD Pipeline](https://github.com/ankitadorsys/github-action/actions/workflows/pipeline.yml/badge.svg)](https://github.com/ankitadorsys/github-action/actions/workflows/pipeline.yml)

A learning project to master **GitHub Actions** by building a full-stack Task Manager
with a Spring Boot backend, Angular frontend, and deployment to **Cloud Foundry**
(SAP BTP) using **Terraform** for infrastructure.

## Live App

https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com/api/hello

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Build Tool | Maven |
| CI/CD | GitHub Actions |
| IaC | Terraform |
| Deployment | Cloud Foundry (SAP BTP) |

## Project Structure

```
github-action/
├── backend/                      # Spring Boot backend (Maven)
├── frontend/                     # Angular frontend
├── api/                          # OpenAPI contract (single source of truth)
├── terraform/                    # Infrastructure as Code
├── .github/workflows/
│   ├── pipeline.yml              # Full CI/CD pipeline
│   └── reusable-build.yml        # Reusable build workflow
├── LEARNING.md                   # Step-by-step learning journal
├── PLAN.md                       # Phase roadmap
└── SKILLS.md                     # AI context file
```

## CI/CD Pipeline

The pipeline runs automatically on every push and PR:

```
Push to feature branch  -->  Matrix build (Java 21 + 25)
Open PR to main         -->  Matrix build + Terraform plan (PR comment)
Merge to main           -->  Matrix build + Terraform apply + deploy to CF
```

## Learning Phases

1. **Spring Boot App** — REST endpoint, tests, CF manifest
2. **Basic CI** — GitHub Actions build & test on push
3. **Manual Deploy** — `workflow_dispatch` + CF CLI deployment
4. **Terraform IaC** — Infrastructure as Code for CF resources
5. **Full Pipeline** — Multi-job CI/CD with Terraform plan/apply
6. **Advanced Workflows** — Reusable workflows, matrix builds, status badges

See [LEARNING.md](LEARNING.md) for detailed notes on each phase.
