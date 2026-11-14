#!/usr/bin/env bash
set -euo pipefail

dnf update -y
dnf install -y docker git cronie
systemctl enable --now docker
systemctl enable --now crond
usermod -aG docker ec2-user

compose_version=v5.7.1
curl -fsSL "https://github.com/docker/compose/releases/download/${compose_version}/docker-compose-linux-x86_64" -o /tmp/docker-compose-linux-x86_64
curl -fsSL "https://github.com/docker/compose/releases/download/${compose_version}/checksums.txt" -o /tmp/docker-compose-checksums.txt
(cd /tmp && grep 'docker-compose-linux-x86_64$' docker-compose-checksums.txt | sha256sum --check --status)
install -m 0755 /tmp/docker-compose-linux-x86_64 /usr/local/bin/docker-compose

install -d -o ec2-user -g ec2-user /opt/study-coach
if [ ! -d /opt/study-coach/.git ]; then
  git clone --branch main --depth 1 https://github.com/TrinhDucDuong/Fasttrack-Java-Dev02-Capstone.git /opt/study-coach
fi

cat > /etc/cron.d/study-coach-backup <<'CRON'
0 18 * * * root bash /opt/study-coach/deploy/backup-db-to-s3.sh >> /var/log/study-coach-backup.log 2>&1
CRON
chmod 0644 /etc/cron.d/study-coach-backup

