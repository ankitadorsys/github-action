# =====================================================================
# outputs.tf — Output Values
# =====================================================================
# Outputs display useful information after `terraform apply` completes.
# They also allow other Terraform modules or CI/CD pipelines to
# reference these values programmatically.
#
# Run `terraform output` anytime to see these values again.
# =====================================================================

output "app_url" {
  description = "The URL of the deployed application"
  value       = "https://${cloudfoundry_route.app_route.endpoint}"
}

output "app_id" {
  description = "The GUID of the CF application"
  value       = cloudfoundry_app.app.id
}

output "frontend_url" {
  description = "The URL of the deployed frontend application"
  value       = "https://${cloudfoundry_route.frontend_route.endpoint}"
}

output "frontend_app_id" {
  description = "The GUID of the frontend CF application"
  value       = cloudfoundry_app.frontend_app.id
}

output "space_id" {
  description = "The GUID of the CF space"
  value       = data.cloudfoundry_space.space.id
}
