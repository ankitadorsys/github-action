# =====================================================================
# variables.tf — Input Variables
# =====================================================================
# Variables are Terraform's way of parameterizing configurations.
# They let you reuse the same .tf files across environments without
# hardcoding values.
#
# Values come from (in order of precedence, highest first):
#   1. CLI flags:         terraform apply -var="cf_password=secret"
#   2. .tfvars file:      terraform apply -var-file="dev.tfvars"
#   3. Environment vars:  TF_VAR_cf_password=secret
#   4. Default value in this file
#   5. Interactive prompt (if no default and not provided)
#
# Sensitive variables (like passwords) should NEVER have defaults.
# =====================================================================

# --------------------------------------------------------------------
# Cloud Foundry Connection Variables
# --------------------------------------------------------------------

variable "cf_api_endpoint" {
  description = "Cloud Foundry API endpoint URL"
  type        = string
  default     = "https://api.cf.ap21.hana.ondemand.com"
}

variable "cf_username" {
  description = "Cloud Foundry username (email address for SAP BTP)"
  type        = string
  sensitive   = true
}

variable "cf_password" {
  description = "Cloud Foundry password"
  type        = string
  sensitive   = true
}

# --------------------------------------------------------------------
# Cloud Foundry Organization & Space
# --------------------------------------------------------------------

variable "cf_org_name" {
  description = "Cloud Foundry organization name"
  type        = string
  default     = "86d1d2ddtrial"
}

variable "cf_space_name" {
  description = "Cloud Foundry space name"
  type        = string
  default     = "dev"
}

# --------------------------------------------------------------------
# Application Variables
# --------------------------------------------------------------------

variable "app_name" {
  description = "Name of the CF application"
  type        = string
  default     = "github-action-demo"
}

variable "app_memory" {
  description = "Memory limit per app instance in MB"
  type        = number
  default     = 768
}

variable "app_instances" {
  description = "Number of application instances"
  type        = number
  default     = 1
}

variable "app_path" {
  description = "Path to the application JAR file (relative to terraform/ directory)"
  type        = string
  default     = "../target/github-action-demo-0.0.1-SNAPSHOT.jar"
}

variable "app_domain" {
  description = "CF shared apps domain"
  type        = string
  default     = "cfapps.ap21.hana.ondemand.com"
}

variable "app_hostname" {
  description = "Hostname for the app route (must be unique across the domain)"
  type        = string
  default     = "github-action-demo-86d1d2ddtrial"
}
