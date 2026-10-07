#!/bin/bash
# restore.sh
# Restores a logical backup of the SmartSpace MySQL database

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <backup-file.sql.gz>"
  exit 1
fi

BACKUP_FILE=$1

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Error: File $BACKUP_FILE not found."
  exit 1
fi

DB_CONTAINER="smartspace-db-prod"
DB_USER=${MYSQL_USER:-root}
DB_PASSWORD=${MYSQL_PASSWORD}
DB_NAME=${MYSQL_DATABASE:-smartspace}

echo "Restoring $BACKUP_FILE to $DB_NAME..."

gunzip < "$BACKUP_FILE" | docker exec -i "$DB_CONTAINER" /usr/bin/mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME"

echo "Restore completed successfully."
