# CHEATSHEET.md — Quick Reference

> 1-2 page cheat sheet. Pin this, refer to it often.

---

## Key Commands

### Maven
```bash
mvn clean verify                   # Full build + all tests (CI uses this)
mvn clean package                  # Build + unit tests only
mvn clean package -DskipTests      # Build only, no tests (deploy shortcut)
```

### Cloud Foundry
```bash
cf auth "<email>" "<password>" --origin sap.ids   # Non-interactive login (CI)
cf target -o 86d1d2ddtrial -s dev                  # Select org + space
cf push                                            # Deploy using manifest.yml
cf apps                                            # List running apps
cf logs github-action-demo --recent                # View recent logs
```

### Terraform
```bash
terraform init                     # Download provider plugins (run first)
terraform plan                     # Dry run — show what would change
terraform apply                    # Apply changes to real infrastructure
terraform output                   # Show outputs (app URL, IDs)
terraform destroy                  # Tear down everything (DANGER)
```

### Git (project conventions)
```bash
git checkout -b feature/07-xxx     # New feature branch
git checkout -b fix/some-bug       # New fix branch
# Commit prefixes: feat: fix: chore: docs: ci:
```

---

## File Map

```
.github/workflows/
├── pipeline.yml           # THE workflow — build, plan, deploy
└── reusable-build.yml     # Reusable build — called by pipeline

terraform/
├── main.tf                # Provider (how to connect)
├── variables.tf           # Inputs (11 variables)
├── cf-resources.tf        # Resources (app + route)
├── outputs.tf             # Outputs (app URL, IDs)
├── terraform.tfvars       # Real values (GITIGNORED!)
└── terraform.tfvars.example  # Safe template (committed)

src/main/java/.../controller/HelloController.java   # GET /api/hello
src/test/java/.../controller/HelloControllerTest.java
pom.xml            # Maven config — Java 17 compile target
manifest.yml       # CF descriptor — Java 21 runtime
```

---

## Pipeline Behavior

| Event | What runs |
|-------|-----------|
| Push to `feature/**` | Matrix build (Java 17 + 21) |
| PR to `main` | Matrix build + Terraform plan (posted as PR comment) |
| Push/merge to `main` | Matrix build + build-for-deploy + Terraform apply + health check |

---

## If X, Then Y (Rules & Gotchas)

| If... | Then... |
|-------|---------|
| CF auth fails with "credentials rejected" | Use **email** (not P-number) + `--origin sap.ids` |
| CF app crashes after deploy | Check JRE version — SAP BTP may not have that JRE |
| Matrix build fails on lower Java version | Set `pom.xml` compile target to the LOWEST matrix version |
| Terraform tries to create duplicates | State file is missing — restore it before `apply` |
| Terraform route causes "already exists" error | Manage route-to-app in the `route` resource's `target` block, not the `app` resource's `routes` block |
| Need plan output in a PR comment | Use `terraform_wrapper: true` in the plan job |
| Need real exit codes from Terraform | Use `terraform_wrapper: false` in the apply/deploy job |
| `cf login` hangs in CI | Use `cf auth` instead (non-interactive) |
| Jobs can't share files | Use `upload-artifact` / `download-artifact` |
| Need to access another branch without switching | Use `git worktree add` |

---

## Version Rule

```
Compile target (pom.xml)    <=    Runtime JRE (CF/manifest)
      Java 17               <=         Java 21             ✓
      Java 21               <=         Java 17             ✗ CRASH
```

Set compile target to the **lowest** version in your test matrix.
Set runtime to whatever's **available** in production.

---

## GitHub Actions Syntax Cheat Sheet

```yaml
# Triggers
on:
  push:
    branches: [main, 'feature/**']
  pull_request:
    branches: [main]
  workflow_dispatch:                    # Manual button
  workflow_call:                        # Reusable (called by another workflow)
    inputs:
      my-input: { type: string, default: 'value' }

# Job conditions
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
if: github.event_name == 'pull_request'

# Matrix
strategy:
  fail-fast: false
  matrix:
    java-version: ['17', '21']

# Job dependency
needs: [build-matrix, build-deploy]

# Secrets
${{ secrets.CF_PASSWORD }}

# Step outputs
echo "key=value" >> "$GITHUB_OUTPUT"           # Write
${{ steps.step-id.outputs.key }}               # Read

# Permissions
permissions:
  contents: write
  pull-requests: write

# Calling a reusable workflow
jobs:
  build:
    uses: ./.github/workflows/reusable-build.yml
    with:
      java-version: '21'
```

---

## URLs & Endpoints

| What | URL |
|------|-----|
| App | `https://github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com` |
| Hello endpoint | `/api/hello` or `/api/hello?name=Ankit` |
| Health check | `/actuator/health` |
| CF API | `https://api.cf.ap21.hana.ondemand.com` |
| GitHub repo | `https://github.com/ankitadorsys/github-action` |
| Pipeline badge | `https://github.com/ankitadorsys/github-action/actions/workflows/pipeline.yml/badge.svg` |

---

## SAP BTP Quick Reference

```
Global Account:  86d1d2ddtrial
Org:             86d1d2ddtrial
Space:           dev
Region:          Singapore - Azure (ap21)
IDP Origin:      sap.ids
Auth user:       email (***REDACTED_EMAIL***), NOT P-number
```

---

## Terraform State in CI

```
1. RESTORE:  git show origin/terraform-state:terraform.tfstate > terraform/terraform.tfstate
2. RUN:      terraform init && terraform apply
3. SAVE:     git worktree add /tmp/tf-state origin/terraform-state
             cp terraform.tfstate /tmp/tf-state/
             git -C /tmp/tf-state add + commit + push
```

State lives on orphan branch `terraform-state` (no shared history with `main`).
