# Terraform - eKYC Rail KMS Signing Key

This stack provisions an AWS KMS asymmetric key for RS256 JWT signing used by `KmsJwtSigner`.

## What it creates
- `aws_kms_key` with `key_usage = SIGN_VERIFY`
- `aws_kms_alias` for stable key reference
- Optional `aws_iam_role` for ECS/Fargate runtime (`create_runtime_iam_role=true`)
- Runtime inline policy for least-privilege KMS use (`kms:Sign`, `kms:Verify`, `kms:GetPublicKey`, `kms:DescribeKey`)
- Optional managed policy attachments via `runtime_role_additional_policy_arns`
- Key policy statements for:
  - account root full access
  - optional admin principals
  - optional runtime signing principals (`kms:Sign`, `kms:Verify`, `kms:GetPublicKey`, `kms:DescribeKey`), including this stack's runtime role when enabled

## Standardized tagging
All taggable resources are stamped with common metadata tags:
- `ProjectName`
- `ProjectVersion`
- `ComponentVersion`
- `ProvisionedDate`
- `Environment`
- `ManagedBy`
- `Service`
- `Owner`
- `Repository`

Set these values in `terraform.tfvars` for release tracking and governance consistency.

## Prerequisites
- Terraform >= 1.6
- AWS credentials available in environment or profile
- AWS IAM permissions to create KMS keys and aliases

## Usage
1. Copy and edit variables:
   - `terraform.tfvars.example` -> `terraform.tfvars`
2. Initialize and apply:

```bash
terraform -chdir=terraform init
terraform -chdir=terraform plan
terraform -chdir=terraform apply
```

## Output wiring to application
After apply, read key ARN:

```bash
terraform -chdir=terraform output kms_key_arn
```

If runtime role provisioning is enabled, also read role ARN:

```bash
terraform -chdir=terraform output runtime_iam_role_arn
```

Use this as environment variable for app startup:

```bash
export AWS_KMS_KEY_ARN=<output-value>
export AWS_REGION=ap-south-1
```

For Windows PowerShell:

```powershell
$env:AWS_KMS_KEY_ARN="<output-value>"
$env:AWS_REGION="ap-south-1"
```

## Notes
- Asymmetric signing keys do not support data-key generation/envelope encryption. Create a separate symmetric KMS key if you later add encryption operations.
- Keep `kms_usage_principal_arns` scoped to explicit external runtime roles when needed.
- The generated runtime role trust policy is for `ecs-tasks.amazonaws.com` and is intended for ECS/Fargate task role usage.

