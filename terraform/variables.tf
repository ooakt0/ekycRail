variable "aws_region" {
  description = "AWS region where the KMS key will be created."
  type        = string
  default     = "ap-south-1"
}

variable "environment" {
  description = "Deployment environment tag value."
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name tag value."
  type        = string
  default     = "ekyc-rail"
}

variable "project_version" {
  description = "Project/application version tag value."
  type        = string
  default     = "0.0.1-SNAPSHOT"
}

variable "component_version" {
  description = "Terraform component version tag value for infra tracking."
  type        = string
  default     = "v1"
}

variable "provisioned_date" {
  description = "Provisioning date tag value (YYYY-MM-DD). Use a fixed release date to avoid drift."
  type        = string
  default     = "2026-08-09"
}

variable "owner" {
  description = "Owning team or unit tag value."
  type        = string
  default     = "platform"
}

variable "repository_name" {
  description = "Source repository name tag value."
  type        = string
  default     = "ekycRail"
}

variable "kms_alias_name" {
  description = "KMS alias name. Must start with alias/."
  type        = string
  default     = "alias/ekyc-rail-signing"

  validation {
    condition     = startswith(var.kms_alias_name, "alias/")
    error_message = "kms_alias_name must start with alias/."
  }
}

variable "kms_key_description" {
  description = "Description for the eKYC Rail signing key."
  type        = string
  default     = "eKYC Rail KMS key for RS256 JWT signing"
}

variable "deletion_window_in_days" {
  description = "Waiting period before scheduled key deletion."
  type        = number
  default     = 30

  validation {
    condition     = var.deletion_window_in_days >= 7 && var.deletion_window_in_days <= 30
    error_message = "deletion_window_in_days must be between 7 and 30."
  }
}

variable "customer_master_key_spec" {
  description = "Customer master key spec for asymmetric signing."
  type        = string
  default     = "RSA_2048"

  validation {
    condition = contains([
      "RSA_2048",
      "RSA_3072",
      "RSA_4096"
    ], var.customer_master_key_spec)
    error_message = "customer_master_key_spec must be one of RSA_2048, RSA_3072, RSA_4096."
  }
}

variable "key_usage" {
  description = "KMS key usage type. Use SIGN_VERIFY for RS256 JWT signing."
  type        = string
  default     = "SIGN_VERIFY"

  validation {
    condition     = var.key_usage == "SIGN_VERIFY"
    error_message = "key_usage is locked to SIGN_VERIFY for JWT signing safety."
  }
}

variable "kms_admin_principal_arns" {
  description = "IAM principal ARNs allowed to administer the key."
  type        = list(string)
  default     = []
}

variable "kms_usage_principal_arns" {
  description = "IAM principal ARNs allowed to sign/verify with the key (for ECS task roles)."
  type        = list(string)
  default     = []
}

variable "create_runtime_iam_role" {
  description = "Whether to provision an ECS task runtime IAM role with KMS signing permissions."
  type        = bool
  default     = true
}

variable "runtime_iam_role_name" {
  description = "Name for the provisioned ECS runtime IAM role."
  type        = string
  default     = "ekyc-rail-task-role"
}

variable "runtime_iam_role_path" {
  description = "Path for the runtime IAM role."
  type        = string
  default     = "/service-role/"
}

variable "runtime_iam_role_description" {
  description = "Description for the runtime IAM role used by ECS/Fargate tasks."
  type        = string
  default     = "Runtime IAM role for eKYC Rail service to sign/verify JWTs with AWS KMS"
}

variable "runtime_role_additional_policy_arns" {
  description = "Optional managed policy ARNs to attach to the runtime IAM role."
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Extra tags to apply to KMS resources."
  type        = map(string)
  default     = {}
}

