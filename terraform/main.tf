provider "aws" {
  region = var.aws_region
}

locals {
  common_tags = merge(
    {
      ProjectName      = var.project_name
      ProjectVersion   = var.project_version
      ProvisionedDate  = var.provisioned_date
      Environment      = var.environment
      ManagedBy        = "terraform"
      Service          = "ekyc-rail"
      ComponentVersion = var.component_version
      Owner            = var.owner
      Repository       = var.repository_name
    },
    var.tags
  )
}

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "runtime_assume_role" {
  statement {
    sid    = "AllowEcsTasksToAssumeRole"
    effect = "Allow"

    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ekyc_runtime" {
  count = var.create_runtime_iam_role ? 1 : 0

  name               = var.runtime_iam_role_name
  path               = var.runtime_iam_role_path
  description        = var.runtime_iam_role_description
  assume_role_policy = data.aws_iam_policy_document.runtime_assume_role.json

  tags = merge(local.common_tags, { Name = var.runtime_iam_role_name })
}

locals {
  kms_usage_principal_arns_effective = concat(
    var.kms_usage_principal_arns,
    var.create_runtime_iam_role ? [aws_iam_role.ekyc_runtime[0].arn] : []
  )
}

data "aws_iam_policy_document" "kms" {
  statement {
    sid    = "AllowAccountRootFullAccess"
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
    }

    actions   = ["kms:*"]
    resources = ["*"]
  }

  dynamic "statement" {
    for_each = length(var.kms_admin_principal_arns) > 0 ? [1] : []

    content {
      sid    = "AllowKeyAdministrators"
      effect = "Allow"

      principals {
        type        = "AWS"
        identifiers = var.kms_admin_principal_arns
      }

      actions = [
        "kms:Create*",
        "kms:Describe*",
        "kms:Enable*",
        "kms:List*",
        "kms:Put*",
        "kms:Update*",
        "kms:Revoke*",
        "kms:Disable*",
        "kms:Get*",
        "kms:Delete*",
        "kms:TagResource",
        "kms:UntagResource",
        "kms:ScheduleKeyDeletion",
        "kms:CancelKeyDeletion"
      ]
      resources = ["*"]
    }
  }

  dynamic "statement" {
    for_each = length(local.kms_usage_principal_arns_effective) > 0 ? [1] : []

    content {
      sid    = "AllowAppRuntimeSigningUsage"
      effect = "Allow"

      principals {
        type        = "AWS"
        identifiers = local.kms_usage_principal_arns_effective
      }

      actions = [
        "kms:Sign",
        "kms:Verify",
        "kms:GetPublicKey",
        "kms:DescribeKey"
      ]
      resources = ["*"]
    }
  }
}

resource "aws_kms_key" "ekyc_signing" {
  description              = var.kms_key_description
  deletion_window_in_days  = var.deletion_window_in_days
  enable_key_rotation      = false
  customer_master_key_spec = var.customer_master_key_spec
  key_usage                = var.key_usage
  policy                   = data.aws_iam_policy_document.kms.json

  tags = merge(local.common_tags, { Name = "ekyc-rail-signing-key" })
}

resource "aws_kms_alias" "ekyc_signing_alias" {
  name          = var.kms_alias_name
  target_key_id = aws_kms_key.ekyc_signing.key_id
}

data "aws_iam_policy_document" "runtime_kms_signing" {
  count = var.create_runtime_iam_role ? 1 : 0

  statement {
    sid    = "AllowKmsSignVerifyForEkycKey"
    effect = "Allow"

    actions = [
      "kms:Sign",
      "kms:Verify",
      "kms:GetPublicKey",
      "kms:DescribeKey"
    ]

    resources = [aws_kms_key.ekyc_signing.arn]
  }
}

resource "aws_iam_role_policy" "runtime_kms_signing" {
  count = var.create_runtime_iam_role ? 1 : 0

  name   = "ekyc-rail-kms-signing"
  role   = aws_iam_role.ekyc_runtime[0].id
  policy = data.aws_iam_policy_document.runtime_kms_signing[0].json
}

resource "aws_iam_role_policy_attachment" "runtime_additional" {
  for_each = var.create_runtime_iam_role ? toset(var.runtime_role_additional_policy_arns) : toset([])

  role       = aws_iam_role.ekyc_runtime[0].name
  policy_arn = each.value
}

