#!/usr/bin/env bash
set -euo pipefail

APP_DIR=/opt/study-coach
REGION="${AWS_REGION:?AWS_REGION is required}"
IMAGE_TAG="${1:?Image tag is required}"
ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"

cd "$APP_DIR"
if ! command -v docker-compose >/dev/null 2>&1; then
  if [ -x /usr/local/lib/docker/cli-plugins/docker-compose ]; then
    install -m 0755 /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose
  else
    compose_version=v5.7.1
    curl -fsSL "https://github.com/docker/compose/releases/download/${compose_version}/docker-compose-linux-x86_64" -o /tmp/docker-compose-linux-x86_64
    curl -fsSL "https://github.com/docker/compose/releases/download/${compose_version}/checksums.txt" -o /tmp/docker-compose-checksums.txt
    (cd /tmp && grep 'docker-compose-linux-x86_64$' docker-compose-checksums.txt | sha256sum --check --status)
    install -m 0755 /tmp/docker-compose-linux-x86_64 /usr/local/bin/docker-compose
  fi
fi

git fetch origin main
git checkout -f main
git reset --hard "$IMAGE_TAG"
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY"

get_parameter() {
  aws ssm get-parameter --region "$REGION" --name "/studycoach/prod/$1" --with-decryption --query 'Parameter.Value' --output text
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
  printf 'ADMIN_EMAIL=%s\n' "$(get_parameter admin-email)"
  printf 'ADMIN_PASSWORD=%s\n' "$(get_parameter admin-password)"
  printf 'JWT_SECRET=%s\n' "$(get_parameter jwt-secret)"
  printf 'RABBITMQ_USERNAME=%s\n' "$(get_parameter rabbitmq-username)"
  printf 'RABBITMQ_PASSWORD=%s\n' "$(get_parameter rabbitmq-password)"
  printf 'GROQ_API_KEY=%s\n' "$(get_parameter groq-api-key)"
  printf 'BACKUP_BUCKET=%s\n' "$(get_parameter backup-bucket)"
  printf 'VIDEO_BUCKET=%s\n' "$(get_parameter video-bucket)"
} > "$env_file"
chmod 600 "$env_file"
mv "$env_file" "$APP_DIR/.env"
trap - EXIT

docker-compose --env-file "$APP_DIR/.env" -f deploy/docker-compose.aws.yml pull
docker-compose --env-file "$APP_DIR/.env" -f deploy/docker-compose.aws.yml up -d --remove-orphans
for attempt in $(seq 1 90); do
  if curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
    docker image prune -af --filter 'until=168h'
    exit 0
  fi
  sleep 5
done
echo "Gateway did not become healthy"
exit 1
