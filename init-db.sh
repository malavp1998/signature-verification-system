#!/bin/bash
set -e

# This script runs as postgres user
# Create the database if it doesn't exist
psql -v ON_ERROR_STOP=1 <<-EOSQL
  -- Create database if it doesn't exist
  CREATE DATABASE sigverify;
EOSQL

# Now create extensions in the sigverify database
psql -v ON_ERROR_STOP=1 -d sigverify <<-EOSQL
  -- Create extensions
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  CREATE EXTENSION IF NOT EXISTS vector;
EOSQL

echo "✅ Database initialized successfully"
