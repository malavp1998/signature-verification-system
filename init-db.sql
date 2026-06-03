-- Initialize database
-- PostgreSQL automatically creates the database from POSTGRES_DB env var
-- This script ensures extensions are enabled

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS vector;
