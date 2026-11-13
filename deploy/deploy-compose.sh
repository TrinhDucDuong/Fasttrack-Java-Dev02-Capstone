#!/usr/bin/env bash
set -euo pipefail

APP_DIR=/opt/study-coach
REGION="${AWS_REGION:?AWS_REGION is required}"
IMAGE_TAG="${1:?Image tag is required}"
ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"

cd "$APP_DIR"
git fetch origin main
git checkout -f main
git reset --hard origin/main
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY"

get_parameter() {
  aws ssm get-parameter --name "/studycoach/prod/$1" --with-decryption --query 'Parameter.Value' --output text
}

env_file="$(mktemp "$APP_DIR/.env.XXXXXX")"
trap 'rm -f "$env_file"' EXIT
{
  printf 'AWS_REGION=%s\n' "$REGION"
  printf 'ECR_REGISTRY=%s\n' "$ECR_REGISTRY"
  printf 'IMAGE_TAG=%s\n' "$IMAGE_TAG"
  printf 'DB_HOST=%s\n' "$(get_parameter db-host)"
  printf 'DB_USERNAME=%s\n' "$(get_parameter db-username)"
  printf 'DB_PASSWORD=%s\n' "$(get_parameter db-password)"
  printf 'JWT_SECRET=%s\n' "$(get_parameter jwt-secret)"
  printf 'RABBITMQ_USERNAME=%s\n' "$(get_parameter rabbitmq-username)"
  printf 'RABBITMQ_PASSWORD=%s\n' "$(get_parameter rabbitmq-password)"
  printf 'GROQ_API_KEY=%s\n' "$(get_parameter groq-api-key)"
  printf 'BACKUP_BUCKET=%s\n' "$(get_parameter backup-bucket)"
} > "$env_file"
chmod 600 "$env_file"
mv "$env_file" "$APP_DIR/.env"
trap - EXIT

docker compose --env-file "$APP_DIR/.env" -f deploy/docker-compose.aws.yml pull
docker compose --env-file "$APP_DIR/.env" -f deploy/docker-compose.aws.yml up -d --remove-orphans
docker image prune -af --filter 'until=168h'

