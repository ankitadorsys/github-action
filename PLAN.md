# PLAN.md — Phased Roadmap & Progress Tracker

## Phase 1: Spring Boot Application
**Branch:** `feature/01-spring-boot-app`
**Goal:** Create a minimal Spring Boot app with a health endpoint, ready for CF deployment.

### Files
- `.gitignore`
- `pom.xml` — Java 21, Spring Boot 3.x, Web + Actuator + Test starters
- `src/main/java/com/example/demo/DemoApplication.java`
- `src/main/java/com/example/demo/controller/HelloController.java` — `GET /api/hello`
- `src/main/resources/application.yml`
- `src/test/java/com/example/demo/controller/HelloControllerTest.java`
- `manifest.yml` — CF deployment descriptor

### What You Learn
- Spring Boot project structure
- Maven build lifecycle
- CF manifest.yml format
- Basic REST endpoints and testing

### Checklist
- [ ] Spring Boot app created and builds
- [ ] `/api/hello` returns a response
- [ ] `/actuator/health` returns UP
- [ ] Unit tests pass
- [ ] `manifest.yml` configured
- [ ] Branch pushed, PR created

---

## Phase 2: Basic CI Workflow
**Branch:** `feature/02-basic-ci-workflow`
**Goal:** Automate build and test on every push and PR.

### Files
- `.github/workflows/ci.yml`

### What You Learn
- GitHub Actions workflow syntax (YAML)
- Triggers: `push`, `pull_request`
- Actions: `actions/checkout`, `actions/setup-java`, `actions/cache`
- Viewing workflow runs in the GitHub Actions tab

### Checklist
- [ ] `ci.yml` created with build + test steps
- [ ] Maven dependency caching configured
- [ ] Workflow triggers on push and PR
- [ ] Workflow runs green on GitHub

---

## Phase 3: Manual CF Deploy Workflow
**Branch:** `feature/03-manual-cf-deploy`
**Goal:** Deploy to Cloud Foundry via a manual button in GitHub.

### Files
- `.github/workflows/deploy.yml`

### What You Learn
- `workflow_dispatch` trigger (manual runs)
- GitHub Secrets usage
- Installing CF CLI in a workflow
- `cf login` + `cf push` in CI

### Prerequisites
- SAP BTP CF environment confirmed working
- GitHub Secrets configured: `CF_API_ENDPOINT`, `CF_USERNAME`, `CF_PASSWORD`, `CF_ORG`, `CF_SPACE`

### Checklist
- [ ] GitHub secrets configured
- [ ] `deploy.yml` created with manual trigger
- [ ] CF CLI installed in workflow
- [ ] App deploys successfully to SAP BTP CF
- [ ] App accessible via CF route URL

---

## Phase 4: Terraform CF Infrastructure
**Branch:** `feature/04-terraform-cf-infra`
**Goal:** Manage CF resources (space, app, routes) as code with Terraform.

### Files
- `terraform/main.tf` — Provider config
- `terraform/variables.tf` — Input variables
- `terraform/cf-resources.tf` — CF space, app, route definitions
- `terraform/outputs.tf` — Output app URL
- `terraform/terraform.tfvars.example` — Example values (no secrets)

### What You Learn
- Terraform basics: providers, resources, variables, outputs
- Cloud Foundry Terraform provider (`cloudfoundry-community/cloudfoundry`)
- `terraform init`, `plan`, `apply`, `destroy`
- State management

### Checklist
- [ ] Terraform configs created
- [ ] `terraform init` succeeds
- [ ] `terraform plan` shows expected resources
- [ ] `terraform apply` deploys app to CF
- [ ] Terraform state is gitignored

---

## Phase 5: Full CI/CD Pipeline
**Branch:** `feature/05-full-cicd-pipeline`
**Goal:** Multi-job pipeline with build, Terraform plan on PR, deploy on merge.

### Files
- `.github/workflows/pipeline.yml`

### What You Learn
- Multi-job workflows with `needs:` dependencies
- Uploading/downloading artifacts between jobs
- Posting Terraform plan as PR comment
- Conditional execution (`if:` expressions)
- GitHub Environments and protection rules

### Checklist
- [ ] Job 1: Build & test, upload JAR artifact
- [ ] Job 2: Terraform plan on PRs, post as PR comment
- [ ] Job 3: Terraform apply + deploy on merge to main
- [ ] Environment protection rules configured
- [ ] Pipeline runs end-to-end

---

## Phase 6: Advanced Workflows
**Branch:** `feature/06-advanced-workflows`
**Goal:** Learn reusable workflows, matrix builds, and advanced patterns.

### Files
- `.github/workflows/reusable-build.yml` — Reusable workflow
- Updates to existing workflows to use reusable workflow

### What You Learn
- Reusable workflows (`workflow_call`)
- Matrix strategy (test Java 17 + 21)
- Workflow status badges in README
- Branch protection rules
- Composite actions vs reusable workflows

### Checklist
- [ ] Reusable build workflow created
- [ ] Matrix build for Java 17 + 21
- [ ] Status badge added to README
- [ ] Branch protection configured
- [ ] All workflows use reusable components where appropriate
