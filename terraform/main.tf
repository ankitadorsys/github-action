# =====================================================================
# main.tf — Terraform Configuration & Provider Setup
# =====================================================================
# This file tells Terraform:
#   1. Which version of Terraform to use
#   2. Which providers (plugins) to download
#   3. How to authenticate with Cloud Foundry
# =====================================================================

# --------------------------------------------------------------------
# Terraform Settings Block
# --------------------------------------------------------------------
# `required_version` ensures everyone uses a compatible Terraform CLI.
# `required_providers` tells Terraform where to download the CF plugin.
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    cloudfoundry = {
      source  = "cloudfoundry-community/cloudfoundry"
      version = "~> 0.53.0"
    }
  }
}

# --------------------------------------------------------------------
# Cloud Foundry Provider
# --------------------------------------------------------------------
# This block configures the connection to your SAP BTP CF environment.
# Sensitive values (password) come from variables — never hardcode them.
#
# The `origin` field is critical for SAP BTP — it tells the UAA server
# which identity provider to authenticate against. For SAP ID Service
# (the default IDP on SAP BTP trial), the origin is "sap.ids".
provider "cloudfoundry" {
  api_url  = var.cf_api_endpoint
  user     = var.cf_username
  password = var.cf_password
  origin   = "sap.ids"
}
