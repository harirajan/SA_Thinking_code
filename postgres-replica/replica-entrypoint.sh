#!/bin/bash
set -e

DATA_DIR="/var/lib/postgresql/data"

if [ -z "$(ls -A "$DATA_DIR" 2>/dev/null)" ]; then
    echo "Data directory empty — cloning from primary..."

    until pg_isready -h "$PRIMARY_HOST" -p "$PRIMARY_PORT" -U "$REPLICATION_USER"; do
        echo "Waiting for primary to be ready..."
        sleep 2
    done

    PGPASSWORD="$REPLICATION_PASSWORD" pg_basebackup \
        -h "$PRIMARY_HOST" \
        -p "$PRIMARY_PORT" \
        -U "$REPLICATION_USER" \
        -D "$DATA_DIR" \
        -Fp -Xs -P -R

    echo "Clone complete."
else
    echo "Data directory already populated — skipping clone."
fi

exec docker-entrypoint.sh postgres
