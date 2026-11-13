#!/usr/bin/env bash
set -euo pipefail

APP_DIR=/opt/study-coach
cd "$APP_DIR"
set -a
. "$APP_DIR/.env"
set +a

backup_name="study-coach-$(date -u +%Y%m%dT%H%M%SZ).dump"
docker run --rm \
  -e PGPASSWORD="$DB_PASSWORD" \
  postgres:16-alpine \
  pg_dump --host "$DB_HOST" --username "$DB_USERNAME" --dbname study_coach --format custom \
  | aws s3 cp - "s3://$BACKUP_BUCKET/postgres/$backup_name" --region "$AWS_REGION" --sse AES256

