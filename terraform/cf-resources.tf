# =====================================================================
# cf-resources.tf — Cloud Foundry Resources
# =====================================================================
# This file defines the actual CF infrastructure:
#   - Data sources: look up existing resources (org, space, domain)
#   - Resources: create/manage new resources (route, app)
#
# Data sources vs Resources:
#   - data "..." reads existing infrastructure (doesn't create anything)
#   - resource "..." creates and manages infrastructure
#
# We use data sources for org, space, and domain because these already
# exist in SAP BTP — we don't want Terraform to create or destroy them.
# We use resources for the route and app because Terraform should own those.
# =====================================================================

# --------------------------------------------------------------------
# Data Sources — Look Up Existing CF Resources
# --------------------------------------------------------------------

# Look up the CF organization by name.
# Returns the org GUID, which we need for the space lookup.
data "cloudfoundry_org" "org" {
  name = var.cf_org_name
}

# Look up the CF space within the org.
# Returns the space GUID, which we need for the app and route.
data "cloudfoundry_space" "space" {
  name     = var.cf_space_name
  org_name = var.cf_org_name
}

# Look up the shared apps domain.
# SAP BTP trial provides "cfapps.ap21.hana.ondemand.com" as the shared domain.
data "cloudfoundry_domain" "apps" {
  name = var.app_domain
}

# --------------------------------------------------------------------
# Resources — Managed by Terraform
# --------------------------------------------------------------------

# Create a route (URL) for the application.
# This maps: <hostname>.<domain> → your app
# Example: github-action-demo-86d1d2ddtrial.cfapps.ap21.hana.ondemand.com
#
# The `target` block maps this route to the app. We manage the mapping
# here (on the route) rather than on the app's `routes` block to avoid
# a provider bug where it tries to re-create existing routes.
resource "cloudfoundry_route" "app_route" {
  domain   = data.cloudfoundry_domain.apps.id
  space    = data.cloudfoundry_space.space.id
  hostname = var.app_hostname

  target {
    app = cloudfoundry_app.app.id
  }
}

# Deploy the Cloud Foundry application.
# This is the equivalent of `cf push` — it uploads the JAR,
# stages it with the Java buildpack, and starts the app.
resource "cloudfoundry_app" "app" {
  name      = var.app_name
  space     = data.cloudfoundry_space.space.id
  memory    = var.app_memory
  instances = var.app_instances
  path      = var.app_path
  buildpack = "java_buildpack"

  # Health check — matches what we had in manifest.yml
  health_check_type          = "http"
  health_check_http_endpoint = "/actuator/health"

  # Set Java 17 via environment variable (same as manifest.yml)
  environment = {
    JBP_CONFIG_OPEN_JDK_JRE = "{ jre: { version: 17.+ } }"
  }
}
