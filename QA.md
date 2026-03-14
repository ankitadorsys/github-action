# QA.md — Flashcard Q&A for Active Recall

> **How to use:** Cover the answer (A:), read the question (Q:), try to answer
> from memory, then check. Grouped by phase so you can drill specific topics.

---

## Table of Contents

1. [Phase 1: Spring Boot App](#phase-1-spring-boot-app)
2. [Phase 2: Basic CI Workflow](#phase-2-basic-ci-workflow)
3. [Phase 3: Manual CF Deploy](#phase-3-manual-cf-deploy)
4. [Phase 4: Terraform CF Infrastructure](#phase-4-terraform-cf-infrastructure)
5. [Phase 5: Full CI/CD Pipeline](#phase-5-full-cicd-pipeline)
6. [Phase 6: Advanced Workflows](#phase-6-advanced-workflows)
7. [Cross-Cutting Concepts](#cross-cutting-concepts)

---

## Phase 1: Spring Boot App

**Q: What is `pom.xml` and what does it control?**
A: Maven build configuration. It defines the Java version, dependencies (libraries),
build plugins, and how to package the app (as a JAR).

---

**Q: What do these three Maven commands do?**
```
mvn clean verify
mvn clean package
mvn clean package -DskipTests
```
A:
- `clean verify` — delete old build, compile, run ALL tests (unit + integration), package JAR
- `clean package` — delete old build, compile, run unit tests, package JAR (skip integration tests)
- `clean package -DskipTests` — delete old build, compile, package JAR, skip ALL tests

---

**Q: What is the output of a Maven build and where does it go?**
A: A runnable JAR file at `target/github-action-demo-0.0.1-SNAPSHOT.jar`.

---

**Q: What does `@RestController` do?**
A: Marks a class as a Spring REST controller — it handles HTTP requests and
automatically serializes return values to JSON.

---

**Q: What does `@GetMapping("/hello")` do?**
A: Maps HTTP GET requests to `/hello` (relative to the class-level `@RequestMapping`)
to the annotated method.

---

**Q: What is `@WebMvcTest(HelloController.class)` in the test file?**
A: A Spring Boot test annotation that loads ONLY the web layer (the controller)
without starting the full application. Faster than `@SpringBootTest`.

---

**Q: What is `MockMvc`?**
A: A Spring testing utility that simulates HTTP requests without starting a real
web server. You call `mockMvc.perform(get("/api/hello"))` instead of making real
HTTP calls.

---

**Q: What is Spring Boot Actuator and why do we need it?**
A: A library that adds operational endpoints like `/actuator/health`. Cloud Foundry
uses the health endpoint to check if the app is alive (health check).

---

**Q: What does `manifest.yml` do?**
A: It's the Cloud Foundry deployment descriptor — tells CF the app name, memory,
instances, JAR path, buildpack, Java version, route, and health check config.

---

**Q: What does `JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 21.+ } }'` mean?**
A: It configures the Java Buildpack (JBP) to use Java 21.x JRE as the runtime.
The `+` means "latest patch version of 21".

---

**Q: What is the Maven build lifecycle order?**
A: `clean` -> `compile` -> `test` -> `package` -> `verify` -> `install` -> `deploy`.
Each phase runs all preceding phases.

---

## Phase 2: Basic CI Workflow

**Q: Where must GitHub Actions workflow files be placed?**
A: `.github/workflows/` — GitHub only recognizes YAML files in this exact directory.

---

**Q: What are the four main concepts in a GitHub Actions workflow?**
A:
1. **Workflow** — the YAML file itself
2. **Trigger** (`on:`) — when it runs (push, PR, schedule, etc.)
3. **Job** — a set of steps running on one machine (runner)
4. **Step** — a single command (`run:`) or pre-built action (`uses:`)

---

**Q: What is a runner?**
A: A virtual machine that executes your workflow. `ubuntu-latest` is a free
Linux VM provided by GitHub.

---

**Q: Why do we need `actions/checkout@v4` as the first step?**
A: Runners start as empty machines with no code. `checkout` clones your
repository onto the runner so subsequent steps can access your files.

---

**Q: What does `cache: 'maven'` in `actions/setup-java` do?**
A: Caches `~/.m2/repository` (Maven's local dependency store) between runs.
First run downloads everything; subsequent runs restore from cache (faster).

---

**Q: What does `-B` mean in `mvn clean verify -B`?**
A: Batch mode — disables interactive prompts. Essential for CI where there's
no human to type input.

---

**Q: What is a GitHub Actions artifact?**
A: A file produced by one job that can be downloaded by other jobs or users.
Think of it as a shared folder between jobs. Uploaded with `upload-artifact`,
downloaded with `download-artifact`.

---

**Q: Why can't Job B just read files from Job A's filesystem?**
A: Each job runs on a DIFFERENT virtual machine. Files from Job A don't exist
on Job B's machine. You must use artifacts to transfer files between jobs.

---

## Phase 3: Manual CF Deploy

**Q: What is `workflow_dispatch`?**
A: A trigger that adds a manual "Run workflow" button in the GitHub Actions tab.
Can accept input parameters (like environment selection).

---

**Q: What is the difference between `cf login` and `cf auth`?**
A:
- `cf login` — interactive, prompts for input (bad for CI)
- `cf auth` — non-interactive, all arguments on the command line (good for CI)

---

**Q: What is the correct CF auth command for SAP BTP trial accounts?**
A: `cf auth "<email>" "<password>" --origin sap.ids`
- Username must be the **email address**, NOT the SAP user ID (P-number)
- Origin must be `sap.ids` (SAP ID Service)

---

**Q: What are GitHub Secrets?**
A: Encrypted values stored in repository settings, injected into workflows at
runtime via `${{ secrets.SECRET_NAME }}`. They are masked in logs (shown as `***`).

---

**Q: What are the 5 CF secrets we configured?**
A:
1. `CF_API_ENDPOINT` — CF API URL
2. `CF_USERNAME` — email address (NOT P-number)
3. `CF_PASSWORD` — SAP BTP password
4. `CF_ORG` — org name
5. `CF_SPACE` — space name

---

**Q: What is the CF deployment flow (3 steps)?**
A:
```
cf auth    -->  cf target       -->  cf push
(login)        (pick org/space)     (upload JAR, stage, start)
```

---

**Q: What is an IDP origin and which one works for SAP BTP trial?**
A: IDP = Identity Provider. The `--origin` flag tells CF which identity provider
to authenticate against. For SAP BTP trial accounts, use `sap.ids` (SAP ID Service).
`sap.default` does NOT work for trial.

---

**Q: What does `needs: build` mean in a job definition?**
A: The job won't start until the `build` job completes successfully. It creates
a dependency chain between jobs.

---

**Q: Why split build and deploy into 2 separate jobs?**
A: Separation of concerns. Build doesn't need CF CLI; deploy doesn't need to
rebuild. If deploy fails, you don't waste time re-building.

---

## Phase 4: Terraform CF Infrastructure

**Q: What is the difference between imperative and declarative?**
A:
- **Imperative** (`cf push`): "Run these commands in this order"
- **Declarative** (Terraform): "Here's what I want — make it happen"

Declarative is reproducible, trackable, and shows drift.

---

**Q: What are the three Terraform commands and what do they do?**
A:
1. `terraform init` — download provider plugins
2. `terraform plan` — show what WOULD change (dry run, no modifications)
3. `terraform apply` — actually make the changes

---

**Q: What is the difference between a Terraform data source and a resource?**
A:
- **Data source** (`data`): read-only lookup of EXISTING infrastructure (e.g., find org GUID)
- **Resource** (`resource`): something Terraform CREATES, UPDATES, and can DESTROY

---

**Q: What is Terraform state and why does it matter?**
A: A JSON file (`terraform.tfstate`) that records what Terraform previously
created (resource IDs, attributes). Without state, Terraform doesn't know what
it already created and would try to create duplicates.

---

**Q: What three things does `terraform plan` compare?**
A:
1. `.tf files` (desired state — what you WANT)
2. `terraform.tfstate` (known state — what Terraform THINKS exists)
3. Cloud Foundry API (actual state — what REALLY exists)

---

**Q: In what order does Terraform process variable values (highest priority first)?**
A:
1. Command line: `-var="key=value"`
2. `.tfvars` file: `-var-file="dev.tfvars"`
3. Environment variable: `TF_VAR_key=value`
4. Default value in `variables.tf`
5. Interactive prompt

---

**Q: What does `sensitive = true` do on a Terraform variable?**
A: Hides the variable's value in Terraform's console output. The value still
exists in state — it's just not printed in plan/apply output.

---

**Q: What are the 4 Terraform files in our project and what does each do?**
A:
- `main.tf` — Provider config (HOW to connect to CF)
- `variables.tf` — Input variables (parameterize everything)
- `cf-resources.tf` — Resources (WHAT to create: app + route)
- `outputs.tf` — Output values (WHAT to display after apply: URLs, IDs)

---

**Q: What is the CF Terraform provider route bug and how did we work around it?**
A: Managing route-to-app mappings via the `app` resource's `routes` block causes
"route already exists" errors. Fix: manage the mapping on the `route` resource's
`target` block instead.

```hcl
# BAD:  resource "cloudfoundry_app" { routes { route = ... } }
# GOOD: resource "cloudfoundry_route" { target { app = ... } }
```

---

**Q: How does Terraform figure out the order to create resources?**
A: From resource references. If Route references App's ID, Terraform knows App
must be created first. This is called the **dependency graph**.

---

## Phase 5: Full CI/CD Pipeline

**Q: What are the 3 jobs in the Phase 5 pipeline and when does each run?**
A:
1. **Build & Test** — ALWAYS (every push and PR)
2. **Terraform Plan** — only on pull requests (posts plan as PR comment)
3. **Deploy** — only on push to main (terraform apply + health check)

---

**Q: What `github` context values control conditional execution?**
A:
- `github.event_name` — `'push'` or `'pull_request'`
- `github.ref` — `'refs/heads/main'`, `'refs/heads/feature/...'`, `'refs/pull/N/merge'`

---

**Q: Write the `if:` condition for "run only when merging to main".**
A: `if: github.event_name == 'push' && github.ref == 'refs/heads/main'`

---

**Q: What workflow permissions did we need and why?**
A:
- `contents: write` — to push terraform state back to the repo
- `pull-requests: write` — to post the terraform plan as a PR comment

---

**Q: What is the Terraform state problem in CI and how did we solve it?**
A: Each CI run starts on a clean machine — no state file. Without state,
Terraform would create duplicate resources. Solution: store state on a
dedicated orphan branch (`terraform-state`) in the same repo.

---

**Q: What is an orphan branch?**
A: A git branch with NO shared history with any other branch. Created with
`git checkout --orphan`. Our `terraform-state` branch has only the state file
and never intersects with `main`.

---

**Q: What is `git worktree` and why do we use it in the pipeline?**
A: `git worktree` lets you check out a different branch into a separate
directory WITHOUT switching your current branch. We use it to access the
`terraform-state` branch (to save updated state) while the main checkout
stays on `main`.

---

**Q: What is `terraform_wrapper` and when do we set it true vs false?**
A:
- `terraform_wrapper: true` — wraps Terraform to capture stdout/stderr as step
  outputs. Used in the **plan** job so we can read the plan text and post it as
  a PR comment.
- `terraform_wrapper: false` — no wrapping, real exit codes. Used in the **deploy**
  job so the job fails properly on errors.

---

**Q: How does the pipeline avoid posting duplicate PR comments?**
A: The `github-script` step first searches for an existing comment from the bot.
If found, it UPDATES that comment. If not, it creates a new one.

---

**Q: What is a GitHub Environment and what can you configure on it?**
A:
A named deployment target (e.g., "production") visible in repo settings.
Configurable: required reviewers, wait timers, branch restrictions. Our pipeline
declares `environment: production` on the deploy job.

---

**Q: Describe the 3-step process for managing Terraform state in CI.**
A:
1. **Restore**: `git fetch` the `terraform-state` branch, `git show` the state
   file, copy it into the `terraform/` directory
2. **Run**: `terraform init` + `apply` (reads and updates the local state file)
3. **Save**: `git worktree add` the state branch to `/tmp`, copy updated state
   file there, `git add` + `commit` + `push` back to `terraform-state`

---

## Phase 6: Advanced Workflows

**Q: What is a reusable workflow and how is it triggered?**
A: A workflow that other workflows can CALL like a function. Triggered by
`workflow_call` (not push/PR). Accepts `inputs` and returns `outputs`.

---

**Q: How do you call a reusable workflow from another workflow?**
A:
```yaml
jobs:
  my-job:
    uses: ./.github/workflows/reusable-build.yml
    with:
      java-version: '21'
      upload-artifact: true
```
Note: `uses:` at the **job level** (not step level).

---

**Q: What is a matrix strategy?**
A: It creates multiple job instances from one definition by expanding variable
combinations. `matrix: { java-version: ['17', '21'] }` creates 2 parallel jobs.

---

**Q: What does `fail-fast: false` do and when should you use it?**
A:
- `fail-fast: true` (default): one failure cancels all other matrix jobs
- `fail-fast: false`: all jobs run to completion regardless of failures

Use `false` when you want to see ALL results (e.g., which Java versions fail).
Use `true` to save CI minutes when failures are likely related.

---

**Q: Why does the pipeline have BOTH `build-matrix` and `build-deploy` jobs?**
A: Matrix creates parallel jobs (Java 17 + 21) but you can't depend on a
SPECIFIC matrix entry. The deploy job needs the JAR from Java 21 only.
So `build-deploy` is a separate non-matrix call that produces the artifact,
while `build-matrix` is for testing both versions.

---

**Q: What is `GITHUB_OUTPUT` and how does it work?**
A: A special file for passing data between steps. A step writes
`echo "key=value" >> "$GITHUB_OUTPUT"`, and subsequent steps read it via
`${{ steps.<step-id>.outputs.key }}`.

---

**Q: What is the difference between a reusable workflow and a composite action?**
A:
| | Reusable Workflow | Composite Action |
|---|---|---|
| Reuses | Entire jobs | Steps within a job |
| Runner | Its own runner | Caller's runner |
| Called at | Job level (`uses:`) | Step level (`uses:`) |
| Trigger | `workflow_call` | None (it's a step) |
| Use when | Complex multi-step processes | Small reusable step sequences |

---

**Q: What is a status badge and what's the URL format?**
A: A live image in README that shows the latest workflow pass/fail status.
Format: `https://github.com/<owner>/<repo>/actions/workflows/<file>/badge.svg`

---

**Q: Why did we delete `ci.yml` and `deploy.yml` in Phase 6?**
A:
- `ci.yml` fired on the SAME triggers as `pipeline.yml` — every push triggered
  TWO workflows doing the same build (wasteful)
- `deploy.yml` used `cf push` (imperative) — replaced by Terraform (declarative)
  in Phase 4-5

---

**Q: What is the difference between compile target and runtime version?**
A:
- **Compile target** (`pom.xml`): what bytecode version to produce. Set to the
  LOWEST version you test against (17 for us).
- **Runtime JRE** (CF/manifest): what JVM runs the app in production. Set to
  whatever's available and stable (21 for us).
- **Rule: Runtime >= Compile target.** Java 21 JRE runs Java 17 bytecode. Not the reverse.

---

**Q: Why did the app crash when we set CF JRE to Java 17?**
A: SAP BTP's Java buildpack didn't have a Java 17 JRE available. The app failed
to start. Fix: keep runtime at 21 (available), only change compile target to 17.

---

**Q: Walk through the Java version debugging timeline (4 steps).**
A:
1. Push with matrix [17, 21] + pom.xml targeting 21 -> Java 17 build FAILS
   ("release version 21 not supported")
2. Fix pom.xml to target 17 -> both builds PASS
3. Merge to main, also changed CF JRE to 17 -> app CRASHES in CF
4. Revert CF JRE to 21 -> app deploys successfully, health check passes

---

## Cross-Cutting Concepts

**Q: What are the 6 phases of this project in order?**
A:
1. Spring Boot App (the app itself)
2. Basic CI Workflow (auto build/test)
3. Manual CF Deploy (button-click deploy)
4. Terraform CF Infrastructure (infra as code)
5. Full CI/CD Pipeline (automated everything)
6. Advanced Workflows (reusable, matrix, polish)

---

**Q: What is the SAP BTP hierarchy?**
A:
```
Global Account (86d1d2ddtrial)
  └── Subaccount (trial)
      └── CF Environment
          └── Org (86d1d2ddtrial)
              └── Space (dev)
                  ├── App (github-action-demo)
                  └── Route (github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com)
```

---

**Q: What branch naming convention do we use?**
A: `feature/<phase-number>-<short-description>` for new work,
`fix/<description>` for bug fixes. Examples: `feature/01-spring-boot-app`,
`fix/cf-auth`.

---

**Q: What commit message convention do we use?**
A: Conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, `ci:`.
The prefix describes the TYPE of change, not the file changed.

---

**Q: Name 3 things that should NEVER be committed to git.**
A:
1. `terraform.tfvars` (has real passwords)
2. `terraform.tfstate` (has infrastructure details, lives on its own branch)
3. `.terraform/` directory (provider binaries, large, re-downloadable)

---

**Q: What is the GITHUB_TOKEN?**
A: An automatically-generated token provided to every workflow run. Has limited
permissions by default. You expand permissions with the `permissions:` block
in the workflow YAML.

---

**Q: Draw the artifact flow between build and deploy jobs.**
A:
```
Build Job (Machine A)           Deploy Job (Machine B)
  mvn package                     (no JAR here!)
  target/app.jar                       |
       |                               |
  upload-artifact ----GitHub----> download-artifact
  name: "app-jar"   storage      name: "app-jar"
  path: target/*.jar              path: target/
                                       |
                                  target/app.jar (now available)
```

---

**Q: What is the complete flow from code push to running app?**
A:
1. Push to feature branch -> matrix build (Java 17 + 21)
2. Open PR to main -> matrix build + terraform plan (posted as PR comment)
3. Merge to main -> matrix build + build-for-deploy (Java 21) + terraform apply + health check
4. App is live at the CF route URL
