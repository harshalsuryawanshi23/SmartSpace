#!/bin/bash
# backup.sh
# Performs a logical backup of the SmartSpace MySQL database

set -e

BACKUP_DIR="/backups/mysql"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DB_CONTAINER="smartspace-db-prod"
DB_USER=${MYSQL_USER:-root}
DB_PASSWORD=${MYSQL_PASSWORD}
DB_NAME=${MYSQL_DATABASE:-smartspace}

mkdir -p "$BACKUP_DIR"

echo "Starting backup of $DB_NAME at $TIMESTAMP..."

docker exec "$DB_CONTAINER" /usr/bin/mysqldump -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" | gzip > "$BACKUP_DIR/${DB_NAME}_backup_${TIMESTAMP}.sql.gz"

echo "Backup completed: $BACKUP_DIR/${DB_NAME}_backup_${TIMESTAMP}.sql.gz"

# Keep only the last 7 days of backups
find "$BACKUP_DIR" -type f -name "${DB_NAME}_backup_*.sql.gz" -mtime +7 -delete

echo "Old backups cleaned up."
