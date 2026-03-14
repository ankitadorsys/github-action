# LEARNING.md — Step-by-Step Learning Journal

> **What is this?** A detailed walkthrough of everything we built, phase by phase.
> Read this to understand *what* was done, *why* it was done, and how all the
> pieces connect together. Written like study notes — not documentation.

---

## Table of Contents

1. [The Big Picture](#the-big-picture)
2. [Phase 1: Spring Boot App](#phase-1-spring-boot-app)
3. [Phase 2: Basic CI Workflow](#phase-2-basic-ci-workflow)
4. [Phase 3: Manual CF Deploy](#phase-3-manual-cf-deploy)
5. [Phase 4: Terraform CF Infrastructure](#phase-4-terraform-cf-infrastructure)
6. [Phase 5: Full CI/CD Pipeline](#phase-5-full-cicd-pipeline)
7. [How Everything Connects](#how-everything-connects)
8. [Git Branch History](#git-branch-history)

---

## The Big Picture

We built a Spring Boot app and progressively automated its build, test, and
deployment using GitHub Actions and Terraform. Each phase added one layer:

```
Phase 1          Phase 2         Phase 3          Phase 4           Phase 5
Spring Boot  --> CI Workflow --> Manual Deploy --> Terraform IaC --> Full Pipeline
(the app)       (auto test)    (button click)   (infra as code)  (fully automated)
```

The final state: **push code to a branch, open a PR, see a Terraform plan,
merge, and the app automatically deploys to Cloud Foundry.**

```
  Developer pushes code
         |
         v
  +------------------+     +-------------------+     +-------------------+
  | GitHub Actions    |     | GitHub Actions    |     | GitHub Actions    |
  | CI/CD Pipeline    |     | (on PR)           |     | (on merge)        |
  |                   |     |                   |     |                   |
  | 1. Build & Test   |---->| 2. Terraform Plan |     | 3. Terraform Apply|
  |    (every push)   |     |    (PR comment)   |     |    + Deploy       |
  +------------------+     +-------------------+     +-------------------+
                                                              |
                                                              v
                                                     +------------------+
                                                     | SAP BTP Cloud    |
                                                     | Foundry (app     |
                                                     | running here)    |
                                                     +------------------+
```

### Where is everything deployed?

```
SAP BTP (Cloud Platform)
└── Global Account: 86d1d2ddtrial
    └── Subaccount: trial
        └── Cloud Foundry Environment
            ├── Org: 86d1d2ddtrial
            │   └── Space: dev
            │       ├── App: github-action-demo (768MB, 1 instance)
            │       └── Route: github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com
            └── CF API: https://api.cf.ap21.hana.ondemand.com
```

---

## Phase 1: Spring Boot App

**Branch:** `feature/01-spring-boot-app`
**Goal:** Create the application we'll deploy in later phases.

### What is Spring Boot?

Spring Boot is a Java framework that makes it easy to create web applications.
It handles all the boilerplate (web server, configuration, dependency injection)
so you can focus on writing your business logic.

### Project Structure

```
github-action/
├── pom.xml                                          # Maven build config
├── manifest.yml                                     # Cloud Foundry deploy config
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java                 # Entry point (main class)
│   │   │   └── controller/
│   │   │       └── HelloController.java             # REST endpoint
│   │   └── resources/
│   │       └── application.yml                      # App configuration
│   └── test/
│       └── java/com/example/demo/controller/
│           └── HelloControllerTest.java             # Unit tests
```

### File-by-File Explanation

#### `pom.xml` — Maven Build Configuration

Maven is the build tool. `pom.xml` is like a recipe that says:
- What Java version to use (21)
- What libraries (dependencies) the app needs
- How to package it (as a JAR file)

```
Key parts:
├── <parent> spring-boot-starter-parent    # Inherits Spring Boot defaults
├── <properties> java.version = 21         # Use Java 21
├── <dependencies>
│   ├── spring-boot-starter-web            # Makes it a web app (HTTP server)
│   ├── spring-boot-starter-actuator       # Adds health check endpoints
│   └── spring-boot-starter-test           # Testing framework
└── <build> spring-boot-maven-plugin       # Packages as runnable JAR
```

**Maven commands we use:**
```bash
mvn clean verify    # Clean old build -> compile -> test -> package JAR
mvn clean package   # Same but skip integration tests
mvn clean package -DskipTests  # Skip ALL tests (faster, used in deploy)
```

The output: `target/github-action-demo-0.0.1-SNAPSHOT.jar` (a runnable JAR file)

#### `HelloController.java` — The REST Endpoint

This is the actual code that handles HTTP requests:

```java
@RestController                    // This class handles HTTP requests
@RequestMapping("/api")            // All URLs start with /api
public class HelloController {

    @GetMapping("/hello")          // Handles GET /api/hello
    public Map<String, Object> hello(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return Map.of(
                "message", "Hello, " + name + "!",
                "timestamp", LocalDateTime.now().toString(),
                "application", "github-action-demo"
        );
    }
}
```

**What it does:** When you visit `https://<app-url>/api/hello`, it returns:
```json
{
  "message": "Hello, World!",
  "timestamp": "2026-03-14T16:49:16",
  "application": "github-action-demo"
}
```

You can also pass a name: `/api/hello?name=Ankit` returns `"Hello, Ankit!"`

#### `application.yml` — App Configuration

```yaml
spring:
  application:
    name: github-action-demo       # App name (used in logs)

server:
  port: 8080                       # Listen on port 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info       # Expose /actuator/health and /actuator/info
  endpoint:
    health:
      show-details: always         # Show detailed health info
```

The `/actuator/health` endpoint is important — Cloud Foundry uses it to check
if the app is alive (health check).

#### `HelloControllerTest.java` — Unit Tests

```java
@WebMvcTest(HelloController.class)    // Only load the web layer for testing
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;           // Simulates HTTP requests without a real server

    @Test
    void hello_withDefaultName_returnsHelloWorld() throws Exception {
        mockMvc.perform(get("/api/hello"))           // Send GET /api/hello
                .andExpect(status().isOk())           // Expect HTTP 200
                .andExpect(jsonPath("$.message").value("Hello, World!"));  // Check JSON
    }
}
```

**Why test?** Tests run automatically in CI. If someone breaks the endpoint,
the build fails before it can be deployed.

#### `manifest.yml` — Cloud Foundry Deployment Descriptor

```yaml
applications:
  - name: github-action-demo                      # App name in CF
    memory: 768M                                   # RAM per instance
    instances: 1                                   # Number of copies
    path: target/github-action-demo-0.0.1-SNAPSHOT.jar  # JAR to deploy
    buildpacks:
      - java_buildpack                             # CF knows how to run Java
    env:
      JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 21.+ } }'  # Use Java 21
    routes:
      - route: github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com
    health-check-type: http
    health-check-http-endpoint: /actuator/health   # CF pings this to check if alive
```

**Think of it like:** "Dear Cloud Foundry, here's my JAR. Give it 768MB of RAM,
use Java 21, and make it available at this URL. Check /actuator/health to know
if it's working."

### What We Learned in Phase 1

- Spring Boot project structure (Maven + Java + resources)
- REST controllers with `@RestController` and `@GetMapping`
- Unit testing with `MockMvc`
- Cloud Foundry manifest format
- Maven build lifecycle (`clean` -> `compile` -> `test` -> `package` -> `verify`)

---

## Phase 2: Basic CI Workflow

**Branch:** `feature/02-basic-ci-workflow`
**Goal:** Automatically build and test on every push — no manual work needed.

### What is GitHub Actions?

GitHub Actions is CI/CD built into GitHub. When you push code, GitHub
automatically runs your workflows (build, test, deploy, etc.) on their servers.

### Key Concepts

```
WORKFLOW (ci.yml)
├── TRIGGER: When does it run? (on push, on PR, on schedule, etc.)
├── JOB: A set of steps that run on one machine
│   ├── RUNNER: The machine (ubuntu-latest = free Linux VM)
│   └── STEPS: Sequential commands
│       ├── ACTION: Pre-built step (actions/checkout@v4)
│       └── RUN: Shell command (mvn clean verify)
```

### File: `.github/workflows/ci.yml`

```
Location matters! GitHub only recognizes workflows in .github/workflows/
```

**The workflow step by step:**

```
┌─ Trigger: push to main or feature/**, OR pull_request to main
│
├─ Job: build-and-test (runs on ubuntu-latest)
│  │
│  ├─ Step 1: actions/checkout@v4
│  │  └── Clones your repo onto the runner (without this, it's an empty machine)
│  │
│  ├─ Step 2: actions/setup-java@v4
│  │  ├── Installs Java 21 (Temurin distribution)
│  │  └── Enables Maven dependency caching (speeds up builds)
│  │
│  ├─ Step 3: mvn clean verify -B --no-transfer-progress
│  │  ├── -B = batch mode (no interactive prompts — important for CI)
│  │  └── --no-transfer-progress = cleaner logs (no download bars)
│  │
│  └─ Step 4: actions/upload-artifact@v4 (only on main)
│     └── Saves the JAR file so other jobs/workflows can use it
```

### What is Maven Caching?

Without caching, Maven downloads ALL dependencies (~100MB) every single run.
With `cache: 'maven'`, GitHub stores `~/.m2/repository` and restores it next run.

```
First run:  Download deps (slow) --> Build --> Cache deps
Next runs:  Restore cache (fast) --> Build
```

### What is an Artifact?

An artifact is a file produced by one job that other jobs can download.
Think of it like a shared folder between jobs.

```
Job 1 (Build)                    Job 2 (Deploy)
  mvn package                      |
  └── target/app.jar               |
      │                            |
      ├── upload-artifact -----> download-artifact
                                   └── target/app.jar
```

### What We Learned in Phase 2

- GitHub Actions workflow YAML syntax
- Triggers: `push`, `pull_request` (with branch filters)
- Actions marketplace: pre-built steps (`checkout`, `setup-java`)
- Maven caching for faster builds
- Artifacts for sharing files between jobs/runs

---

## Phase 3: Manual CF Deploy

**Branch:** `feature/03-manual-cf-deploy`
**Goal:** Add a "Deploy" button in GitHub that deploys to Cloud Foundry.

### What is `workflow_dispatch`?

Most workflows trigger automatically. `workflow_dispatch` adds a manual
"Run workflow" button in the GitHub Actions tab:

```
GitHub Actions tab
└── "Deploy to Cloud Foundry" workflow
    └── [Run workflow] button
        └── Choose: environment = dev
            └── Click "Run workflow"
```

### The 2-Job Architecture

```
┌──────────────────┐         ┌──────────────────┐
│  Job 1: Build    │         │  Job 2: Deploy   │
│                  │         │                  │
│  1. Checkout     │         │  1. Checkout     │
│  2. Setup Java   │  JAR    │  2. Download JAR │
│  3. mvn package  │-------->│  3. Install CF   │
│  4. Upload JAR   │artifact │  4. CF login     │
│                  │         │  5. CF push      │
│                  │         │  6. Verify       │
└──────────────────┘         └──────────────────┘
                    needs: build
```

**Why 2 jobs?** Separation of concerns. Build doesn't need CF CLI.
Deploy doesn't need to rebuild. If deploy fails, you don't re-build.

### GitHub Secrets

Secrets are encrypted values stored in your repository settings.
They're injected into workflows at runtime and NEVER shown in logs.

```
Repository Settings > Secrets and variables > Actions
├── CF_API_ENDPOINT = https://api.cf.ap21.hana.ondemand.com
├── CF_USERNAME     = ***REDACTED_EMAIL***
├── CF_PASSWORD     = ********
├── CF_ORG          = 86d1d2ddtrial
└── CF_SPACE        = dev
```

In the workflow: `${{ secrets.CF_PASSWORD }}` — GitHub replaces this at runtime
and masks the value in logs (shows `***`).

### Cloud Foundry Deployment Flow

```
cf auth     -->  cf target   -->  cf push
(login)         (pick org/      (upload JAR,
                 space)          stage, start)

"Who am I?"    "Where do I     "Deploy this
                deploy?"        app here"
```

### PROBLEM: CF Authentication Failures

This was the hardest part. We went through 4 fix attempts:

```
Attempt 1: cf login with username/password
  ERROR: "Credentials were rejected"
  WHY: SAP BTP uses an Identity Provider (IDP), not simple login

Attempt 2: Added --origin sap.ids
  ERROR: Still rejected
  WHY: Password had special characters, shell was mangling them

Attempt 3: Used env var for password (CF_PASSWORD="${{ secrets.CF_PASSWORD }}")
  ERROR: "Origin sap.default not found" (tried wrong origin)
  WHY: We tried multiple origins but missed the right combination

Attempt 4 (THE FIX):
  - Use `cf auth` (not `cf login`) — it's non-interactive
  - Username must be EMAIL (***REDACTED_EMAIL***), NOT P-number (***REDACTED_ID***)
  - Pass password via environment variable (avoids shell escaping issues)
  - Use --origin sap.ids (SAP ID Service = the identity provider)
```

**The working command:**
```bash
cf auth "${{ secrets.CF_USERNAME }}" "$CF_PASSWORD" --origin sap.ids
```

**Key lesson:** `cf auth` vs `cf login`:
```
cf login  = interactive (prompts for input — bad for CI)
cf auth   = non-interactive (pass everything as arguments — good for CI)
```

### What is an IDP Origin?

SAP BTP can authenticate users through different identity providers.
The `--origin` flag tells CF which one to use:

```
SAP BTP Authentication
├── sap.ids (SAP ID Service) <-- This is the one for trial accounts
├── sap.default              <-- Does NOT work for trial
└── (custom LDAP/SAML)       <-- Enterprise setups
```

### Git History for Phase 3 (shows the struggle)

```
9aa1985 ci: add manual Cloud Foundry deployment workflow        # Initial attempt
aec6545 fix: add --origin sap.ids to CF login                   # Try 1
efd218f fix: use cf auth with env var for special chars          # Try 2
0fa2082 fix: try CF auth without --origin flag + debug           # Try 3
c4a36e0 fix: try multiple IDP origins                           # Try 4
d41400b fix: use correct CF auth with sap.ids origin            # THE FIX (on fix/cf-auth)
```

### What We Learned in Phase 3

- `workflow_dispatch` for manual triggers with input parameters
- GitHub Secrets for storing credentials securely
- Installing tools (CF CLI) on a GitHub runner
- `cf auth` for non-interactive authentication (CI-friendly)
- SAP BTP IDP origins (`sap.ids` for trial accounts)
- Debugging in CI: add echo/debug steps, check logs, iterate
- Multi-job workflows with `needs:` dependency

---

## Phase 4: Terraform CF Infrastructure

**Branch:** `feature/04-terraform-cf-infra`
**Goal:** Manage Cloud Foundry resources as code instead of manual `cf push`.

### Why Terraform?

Before Terraform, we deployed with `cf push` (Phase 3). That works, but:

```
cf push (imperative)                 Terraform (declarative)
"Run these commands in order"        "Here's what I want — make it happen"

cf create-route ...                  resource "cloudfoundry_route" {
cf push --no-route ...                 hostname = "my-app"
cf map-route ...                     }

- No record of what exists           - State file tracks everything
- Hard to reproduce                  - Run again = same result
- "Did someone change something?"    - `terraform plan` shows drift
```

### Terraform Concepts

```
┌─────────────────────────────────────────────────────┐
│                    Terraform                         │
│                                                      │
│  .tf files          terraform plan         Cloud     │
│  (desired state) --> (diff) ------------>  Foundry   │
│       │                                   (actual)   │
│       │           terraform apply                    │
│       └──────────> (make changes) ------>            │
│                                                      │
│  terraform.tfstate                                   │
│  (what Terraform thinks exists)                      │
└─────────────────────────────────────────────────────┘
```

**The 3-step workflow:**
```
terraform init    -->  terraform plan    -->  terraform apply
(download plugins)    (show what will       (actually make
                       change — DRY RUN)     the changes)
```

### File Structure

```
terraform/
├── main.tf                  # Provider config (HOW to connect to CF)
├── variables.tf             # Input variables (parameterize everything)
├── cf-resources.tf          # Resources (WHAT to create in CF)
├── outputs.tf               # Output values (WHAT to display after apply)
├── terraform.tfvars         # Actual values (GITIGNORED — has password)
└── terraform.tfvars.example # Safe template (committed — shows format)
```

### File-by-File Explanation

#### `main.tf` — Provider Configuration

```
terraform {
  required_version = ">= 1.5.0"        # Terraform CLI version
  required_providers {
    cloudfoundry = {
      source  = "cloudfoundry-community/cloudfoundry"   # Plugin from registry
      version = "~> 0.53.0"                             # Compatible version
    }
  }
}

provider "cloudfoundry" {               # How to connect to CF
  api_url  = var.cf_api_endpoint        # https://api.cf.ap21.hana.ondemand.com
  user     = var.cf_username            # Email address
  password = var.cf_password            # From terraform.tfvars (gitignored)
  origin   = "sap.ids"                  # SAP ID Service (same as Phase 3 fix)
}
```

**Think of it like:** "Download the Cloud Foundry plugin, and here's how to log in."

#### `variables.tf` — Input Variables

Variables let you reuse the same `.tf` files with different values.

```
Where do variable values come from? (highest priority first)

1. Command line:    terraform apply -var="cf_password=secret"
2. .tfvars file:    terraform apply -var-file="dev.tfvars"
3. Environment:     export TF_VAR_cf_password=secret
4. Default value:   default = "github-action-demo" (in variables.tf)
5. Prompt:          Terraform asks you interactively
```

We have 11 variables:
```
Connection:   cf_api_endpoint, cf_username, cf_password
Org/Space:    cf_org_name, cf_space_name
App config:   app_name, app_memory, app_instances, app_path
Routing:      app_domain, app_hostname
```

Sensitive ones (`cf_username`, `cf_password`) have `sensitive = true` so
Terraform hides them in output.

#### `cf-resources.tf` — The Actual Infrastructure

Two types of blocks:

```
DATA SOURCES (read-only lookups)          RESOURCES (Terraform manages these)
"Find existing things"                    "Create and manage these things"

data "cloudfoundry_org" "org" {           resource "cloudfoundry_app" "app" {
  name = "86d1d2ddtrial"                    name      = "github-action-demo"
}                                           space     = data...space.id
  ^-- doesn't create anything               memory    = 768
  ^-- just gets the org's GUID              path      = "../target/app.jar"
                                            buildpack = "java_buildpack"
                                          }
                                            ^-- Terraform creates, updates,
                                                and can destroy this
```

**The resource dependency chain:**
```
data.cloudfoundry_org.org
        │
        v (org GUID)
data.cloudfoundry_space.space ──────> resource.cloudfoundry_app.app
        │                                      │
        v (space GUID)                         v (app GUID)
data.cloudfoundry_domain.apps ──────> resource.cloudfoundry_route.app_route
```

Terraform figures out the order automatically from these references.

#### `outputs.tf` — Display After Apply

```
output "app_url" {
  value = "https://${cloudfoundry_route.app_route.endpoint}"
}
```

After `terraform apply`, you see:
```
app_url  = "https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com"
app_id   = "66b9cf63-8f08-489c-bc1f-1b125ead57f2"
space_id = "7a3e1eb0-1670-4661-9cfe-2246c02ff660"
```

### Terraform State

This is the most important concept to understand:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  .tf files   │     │ terraform    │     │  Cloud       │
│  (desired)   │     │ .tfstate     │     │  Foundry     │
│              │     │ (known)      │     │  (actual)    │
│ "I want an   │     │ "Last time I │     │ "What really │
│  app with    │     │  created app │     │  exists      │
│  768MB"      │     │  id=abc123"  │     │  right now"  │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       └────────────────────┼────────────────────┘
                            │
                    terraform plan
                    (compares all 3)
```

- **State file knows** what Terraform previously created (resource IDs, etc.)
- **Plan compares** desired (.tf) vs known (state) vs actual (CF API)
- **Without state**, Terraform doesn't know what it already created and would
  try to create duplicates

### PROBLEM: Route Provider Bug

The CF Terraform provider has a bug where managing route-to-app mappings
on the `app` resource (via `routes` block) causes issues — it tries to
re-create routes that already exist.

**Solution:** Manage the mapping on the `route` resource instead:

```hcl
# BAD — causes "route already exists" errors
resource "cloudfoundry_app" "app" {
  routes { route = cloudfoundry_route.app_route.id }  # Don't do this
}

# GOOD — manages mapping on the route side
resource "cloudfoundry_route" "app_route" {
  target { app = cloudfoundry_app.app.id }            # Do this instead
}
```

### What We Learned in Phase 4

- Terraform basics: providers, resources, data sources, variables, outputs
- Declarative vs imperative infrastructure management
- `terraform init` / `plan` / `apply` workflow
- State management (what it is, why it matters)
- Cloud Foundry Terraform provider specifics
- Working around provider bugs

---

## Phase 5: Full CI/CD Pipeline

**Branch:** `feature/05-full-cicd-pipeline`
**Goal:** Combine everything into one automated pipeline.

### The Problem We're Solving

Before Phase 5, we had:
- `ci.yml` — builds and tests on push (Phase 2)
- `deploy.yml` — manual deploy button with `cf push` (Phase 3)
- Terraform configs — but only run locally (Phase 4)

We want ONE pipeline that does:
```
Push to feature branch  -->  Build & test
Open PR to main         -->  Build & test + show Terraform plan
Merge to main           -->  Build & test + Terraform apply + deploy
```

### The Pipeline Architecture

```
pipeline.yml — ONE workflow, THREE jobs, conditional execution

┌─────────────────────────────────────────────────────────────────┐
│ TRIGGER: push to main/feature/** OR pull_request to main        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              v
              ┌───────────────────────────────┐
              │     Job 1: Build & Test       │
              │     (ALWAYS runs)             │
              │                               │
              │  1. Checkout code              │
              │  2. Setup Java 21             │
              │  3. mvn clean verify          │
              │  4. Upload JAR (main only)    │
              └───────────┬───────────────────┘
                          │
                          │ needs: build
                          │
            ┌─────────────┴──────────────┐
            │                            │
            v                            v
  ┌─────────────────────┐    ┌──────────────────────────┐
  │ Job 2: TF Plan      │    │ Job 3: Deploy            │
  │ (PRs only)          │    │ (push to main only)      │
  │                     │    │                          │
  │ if: event ==        │    │ if: event == 'push'     │
  │     'pull_request'  │    │  && ref == 'main'       │
  │                     │    │                          │
  │ 1. Checkout         │    │ 1. Checkout              │
  │ 2. Restore state    │    │ 2. Download JAR artifact │
  │ 3. TF init          │    │ 3. Restore TF state     │
  │ 4. TF plan          │    │ 4. TF init + apply      │
  │ 5. Post PR comment  │    │ 5. Save state to branch │
  └─────────────────────┘    │ 6. Health check         │
                              └──────────────────────────┘
```

**Key insight:** Jobs 2 and 3 NEVER run at the same time. It's either
a PR (Job 2) or a push to main (Job 3), never both.

### Conditional Execution (`if:` expressions)

This is how we control which jobs run:

```yaml
# Job 2 — only on pull requests
if: github.event_name == 'pull_request'

# Job 3 — only on push to main
if: github.event_name == 'push' && github.ref == 'refs/heads/main'

# Upload artifact — only on push to main (inside Job 1)
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```

**What `github.event_name` and `github.ref` are:**
```
Event: push to feature/05-...    Event: PR to main          Event: merge to main
  event_name = 'push'              event_name = 'pull_...'    event_name = 'push'
  ref = 'refs/heads/feature/05'    ref = 'refs/pull/6/merge'  ref = 'refs/heads/main'

  Result: Job 1 only               Result: Job 1 + Job 2     Result: Job 1 + Job 3
```

### Workflow Permissions

By default, the `GITHUB_TOKEN` (auto-provided to every workflow) has limited
permissions. We need more:

```yaml
permissions:
  contents: write        # To push terraform state back to the repo
  pull-requests: write   # To post the terraform plan as a PR comment
```

### Artifact Passing Between Jobs

Each job runs on a DIFFERENT machine. They can't share files directly.
Artifacts are the bridge:

```
Job 1 (Machine A)                    Job 3 (Machine B)
┌──────────────────┐                 ┌──────────────────┐
│ mvn clean verify │                 │                  │
│ target/app.jar   │                 │ target/ (empty!) │
│       │          │                 │       ^          │
│   upload-artifact│ -- GitHub's --> │   download-      │
│   name: app-jar  │    storage     │   artifact       │
│   path: target/  │                │   name: app-jar  │
│       *.jar      │                │   path: target/  │
└──────────────────┘                 └──────────────────┘
```

### The Terraform State Problem in CI

**The problem:** Each GitHub Actions run starts with a clean machine.
There's no local `terraform.tfstate` file. Without state, Terraform
doesn't know what it previously created.

```
Run 1: terraform apply  -->  Creates app (state: app_id=abc123)
                              State saved to... where?
Run 2: terraform apply  -->  No state file!
                              Terraform: "I don't know about any app"
                              Tries to create a DUPLICATE app!
```

**Our solution:** Store state on a dedicated `terraform-state` git branch.

```
Git branches:
├── main                    # Source code lives here
├── feature/05-...          # Feature work
└── terraform-state         # ONLY has terraform.tfstate (orphan branch)
        │
        ├── README.md
        └── terraform.tfstate
```

**Why an orphan branch?** It has NO shared history with `main`. It's like
a completely separate "folder" in the same repo. The state file never
appears in `main` — it lives only on this branch.

**How the pipeline uses it:**

```
Step 1: RESTORE state                Step 2: RUN Terraform
┌─────────────────────────┐          ┌─────────────────────────┐
│ git fetch origin         │          │ terraform init           │
│   terraform-state        │          │ terraform apply          │
│                          │          │                          │
│ git show origin/         │          │ (reads terraform.tfstate │
│   terraform-state:       │ -------> │  that we restored in     │
│   terraform.tfstate      │          │  Step 1)                 │
│ > terraform/             │          │                          │
│   terraform.tfstate      │          │ (updates terraform.      │
└─────────────────────────┘          │  tfstate with any changes)│
                                      └────────────┬────────────┘
                                                   │
Step 3: SAVE state back                            │
┌─────────────────────────┐                        │
│ git worktree add         │ <─────────────────────┘
│   /tmp/tf-state          │
│   origin/terraform-state │
│                          │
│ cp terraform.tfstate     │
│   /tmp/tf-state/         │
│                          │
│ git add + commit + push  │
│   to terraform-state     │
└─────────────────────────┘
```

**What is `git worktree`?** It lets you check out a different branch into
a separate directory WITHOUT switching your current branch. This way we
can work with the `terraform-state` branch while our main checkout stays
on `main`.

### The Terraform Plan PR Comment

When you open a PR, the pipeline runs `terraform plan` and posts the
output as a comment on the PR:

```
PR #6: "feat: Full CI/CD Pipeline"
├── Commits: e53a57e
├── Checks: ✓ Build & Test, ✓ Terraform Plan
└── Comments:
    └── github-actions[bot]:
        "### Terraform Plan Output
         No changes. Your infrastructure matches the configuration."
```

**How it works (step by step):**

```
1. terraform plan -no-color        # Run plan, capture output as text
        │
        v (stdout captured by terraform_wrapper)
2. steps.plan.outputs.stdout       # Access the output in next step
        │
        v
3. actions/github-script@v7        # Run JavaScript with GitHub API access
        │
        ├── Check: does a plan comment already exist?
        │   ├── YES: Update the existing comment (no duplicates)
        │   └── NO:  Create a new comment
        │
        v
4. PR shows the plan comment       # Reviewer can see infra changes
```

**Why `terraform_wrapper: true`?** The `hashicorp/setup-terraform` action
has a wrapper that captures `stdout` and `stderr` as step outputs. Without
it, we can't access the plan text in the next step.

**Why `terraform_wrapper: false` in the deploy job?** The wrapper changes
exit codes. For `apply`, we need real exit codes (0=success, 1=fail) so
the job fails properly on errors.

### GitHub Environments

```yaml
environment:
  name: production
  url: https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com
```

This creates a "production" environment in GitHub:
- Visible in repo Settings > Environments
- Shows deployment history
- You can add protection rules:
  - Required reviewers (someone must approve before deploy)
  - Wait timer (e.g., 5 minutes before deploy starts)
  - Branch restrictions (only deploy from `main`)

### The Deploy Verification

After Terraform apply, we verify the app is healthy:

```
1. terraform output         # Show app URL, app ID
2. sleep 10                 # Wait for app to stabilize after deploy
3. curl /actuator/health    # Check health endpoint (expect HTTP 200)
4. curl /api/hello          # Check business endpoint (expect JSON response)
```

### What the Workflow Replaced

```
BEFORE (Phase 2-3):                    AFTER (Phase 5):
┌──────────────────┐                   ┌──────────────────────────────┐
│ ci.yml           │                   │ pipeline.yml                 │
│ - Build & test   │                   │ - Build & test (Job 1)       │
└──────────────────┘                   │ - Terraform plan on PR (Job 2)│
┌──────────────────┐                   │ - TF apply + deploy (Job 3)  │
│ deploy.yml       │                   └──────────────────────────────┘
│ - Manual button  │
│ - cf push        │                   One file, fully automated,
└──────────────────┘                   no manual steps needed.
```

Note: `ci.yml` and `deploy.yml` still exist and work. They're not deleted
— just superseded by `pipeline.yml`. We may clean them up in Phase 6.

### What We Learned in Phase 5

- Multi-job workflows with `needs:` dependencies
- Conditional execution with `if:` expressions
- Artifact passing between jobs (upload/download)
- Terraform state management in CI (dedicated branch approach)
- `git worktree` for working with multiple branches simultaneously
- Posting PR comments via `actions/github-script`
- Terraform wrapper for capturing plan output
- GitHub Environments for deployment tracking
- Workflow permissions (`contents: write`, `pull-requests: write`)

---

## How Everything Connects

### The Complete Flow (from code change to running app)

```
YOU                  GITHUB                    GITHUB ACTIONS              SAP BTP CF
 │                     │                           │                          │
 │  git push           │                           │                          │
 │  (feature branch)   │                           │                          │
 │────────────────────>│                           │                          │
 │                     │  trigger: push             │                          │
 │                     │──────────────────────────>│                          │
 │                     │                           │  Job 1: Build & Test     │
 │                     │                           │  (mvn clean verify)      │
 │                     │                           │                          │
 │  Open PR            │                           │                          │
 │────────────────────>│                           │                          │
 │                     │  trigger: pull_request     │                          │
 │                     │──────────────────────────>│                          │
 │                     │                           │  Job 1: Build & Test     │
 │                     │                           │  Job 2: Terraform Plan   │
 │                     │     PR comment            │     │                    │
 │                     │<──────────────────────────│<────┘                    │
 │                     │  "No changes" or          │                          │
 │                     │  "Will create/modify..."  │                          │
 │                     │                           │                          │
 │  Merge PR           │                           │                          │
 │────────────────────>│                           │                          │
 │                     │  trigger: push (main)      │                          │
 │                     │──────────────────────────>│                          │
 │                     │                           │  Job 1: Build & Test     │
 │                     │                           │  Job 3: Deploy           │
 │                     │                           │    terraform apply ──────>│ Update app
 │                     │                           │    save state             │
 │                     │                           │    health check ─────────>│ 200 OK
 │                     │                           │                          │
 │  Visit app URL      │                           │                          │
 │────────────────────────────────────────────────────────────────────────────>│
 │                     │                           │                          │
 │<───── {"message": "Hello, World!", ...} ───────────────────────────────────│
```

### The File Map (what each file does)

```
github-action/
│
├── Source Code (Phase 1)
│   ├── pom.xml                     # "How to build" — Maven recipe
│   ├── manifest.yml                # "How to deploy" — CF descriptor (legacy, Phase 3)
│   └── src/                        # Java source + tests
│
├── CI/CD Workflows (Phase 2, 3, 5)
│   └── .github/workflows/
│       ├── ci.yml                  # Simple build+test (Phase 2, still works)
│       ├── deploy.yml              # Manual deploy button (Phase 3, still works)
│       └── pipeline.yml            # Full pipeline (Phase 5, THE main workflow)
│
├── Infrastructure as Code (Phase 4)
│   └── terraform/
│       ├── main.tf                 # Provider: how to connect to CF
│       ├── variables.tf            # Inputs: parameterized config
│       ├── cf-resources.tf         # Resources: app + route definitions
│       ├── outputs.tf              # Outputs: app URL, IDs
│       └── terraform.tfvars        # Values: GITIGNORED (has password)
│
├── Context (for AI sessions)
│   ├── SKILLS.md                   # Project state & decisions
│   ├── PLAN.md                     # Phase roadmap & checklists
│   └── LEARNING.md                 # This file — learning notes
│
└── Git Branches
    ├── main                        # Production code
    └── terraform-state             # Terraform state file (orphan branch)
```

---

## Git Branch History

Every phase was built on a feature branch, then merged to `main`.
Branches are kept (not deleted) so you can review each phase's changes.

```
main  ─────*───────*──────*──────*──────*──────*──────*──────*──> (current)
           │       │      │      │      │      │      │      │
           │       │      │      │      │    merge   docs   docs
           │       │      │      │      │    PR #6   Phase5  Phase4
           │       │      │      │      │      │
           │       │      │      │    fix/cf-auth (direct merge)
           │       │      │      │
           │       │      │    feature/03-manual-cf-deploy
           │       │      │      (PR #2, #3, #4, #5 — multiple fix attempts)
           │       │      │
           │       │    feature/02-basic-ci-workflow (PR #1)
           │       │
           │     feature/01-spring-boot-app (direct commit to main)
           │
         first commit (empty repo)


terraform-state (orphan — no shared history with main)
  ──*──────*──────*──>
    │      │      │
  init   seed   updated by
  README  state  CI/CD pipeline
```

### All Branches

| Branch | Phase | Status | PRs |
|--------|-------|--------|-----|
| `feature/01-spring-boot-app` | 1 | Merged | (direct) |
| `feature/02-basic-ci-workflow` | 2 | Merged | PR #1 |
| `feature/03-manual-cf-deploy` | 3 | Merged | PR #2, #3, #4, #5 |
| `fix/cf-auth` | 3 (fix) | Merged | (direct) |
| `feature/04-terraform-cf-infra` | 4 | Merged | (direct) |
| `feature/05-full-cicd-pipeline` | 5 | Merged | PR #6 |
| `terraform-state` | 5 | Active | N/A (orphan) |

---

## Glossary

| Term | Meaning |
|------|---------|
| **CI** | Continuous Integration — automatically build and test on every push |
| **CD** | Continuous Deployment — automatically deploy after tests pass |
| **Runner** | The machine (VM) that executes your workflow |
| **Action** | A pre-built reusable step (e.g., `actions/checkout@v4`) |
| **Artifact** | A file passed between jobs (uploaded then downloaded) |
| **Secret** | An encrypted variable stored in GitHub, injected at runtime |
| **IDP** | Identity Provider — the system that verifies your username/password |
| **Origin** | Which IDP to use (for SAP BTP: `sap.ids`) |
| **Terraform State** | A JSON file tracking what Terraform has created (resource IDs, etc.) |
| **Data Source** | Terraform block that reads existing infrastructure (doesn't create) |
| **Resource** | Terraform block that creates/manages infrastructure |
| **Orphan Branch** | A git branch with no shared history with other branches |
| **Worktree** | A git feature to check out multiple branches in separate directories |
| **Environment** | A GitHub feature for tracking deployments with optional protection rules |
