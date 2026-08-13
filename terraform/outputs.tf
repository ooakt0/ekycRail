output "kms_key_arn" {
  description = "ARN of the KMS signing key to set as AWS_KMS_KEY_ARN."
  value       = aws_kms_key.ekyc_signing.arn
}

output "kms_key_id" {
  description = "Key ID of the KMS signing key."
  value       = aws_kms_key.ekyc_signing.key_id
}

output "kms_alias_name" {
  description = "Alias name attached to the signing key."
  value       = aws_kms_alias.ekyc_signing_alias.name
}

output "runtime_iam_role_arn" {
  description = "ARN of provisioned ECS runtime IAM role (null when create_runtime_iam_role=false)."
  value       = try(aws_iam_role.ekyc_runtime[0].arn, null)
}

output "runtime_iam_role_name" {
  description = "Name of provisioned ECS runtime IAM role (null when create_runtime_iam_role=false)."
  value       = try(aws_iam_role.ekyc_runtime[0].name, null)
}

