-- ============================================================
-- Customer Service - Database Initialization Script
-- Tables: customers, addresses, documents, audit_logs, outbox_events
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- CUSTOMERS
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name       CHARACTER VARYING(100) NOT NULL,
    last_name        CHARACTER VARYING(100) NOT NULL,
    identity_number  CHARACTER VARYING(20)  NOT NULL UNIQUE,
    status           CHARACTER VARYING(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ADDRESSES
-- ============================================================
CREATE TABLE IF NOT EXISTS addresses (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id  UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    city         CHARACTER VARYING(100) NOT NULL,
    district     CHARACTER VARYING(100),
    postal_code  CHARACTER VARYING(20),
    is_default   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_addresses_customer_id ON addresses(customer_id);

-- ============================================================
-- DOCUMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS documents (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id  UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    type         CHARACTER VARYING(50)  NOT NULL,
    file_ref     CHARACTER VARYING(255) NOT NULL,
    verified_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_documents_customer_id ON documents(customer_id);

-- ============================================================
-- AUDIT LOGS
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    table_name  CHARACTER VARYING(50)  NOT NULL,
    record_id   UUID NOT NULL,
    action      CHARACTER VARYING(10)  NOT NULL,  -- INSERT, UPDATE, DELETE
    old_data    JSONB,
    new_data    JSONB,
    changed_by  CHARACTER VARYING(100),
    changed_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_record_id   ON audit_logs(record_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_table_name  ON audit_logs(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_logs_changed_at  ON audit_logs(changed_at);

-- ============================================================
-- OUTBOX EVENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS outbox_events (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type CHARACTER VARYING(100) NOT NULL,
    aggregate_id   CHARACTER VARYING(100) NOT NULL,
    type           CHARACTER VARYING(100) NOT NULL,
    payload        JSONB NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at   TIMESTAMP WITH TIME ZONE,
    status         CHARACTER VARYING(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status       ON outbox_events(status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate_id ON outbox_events(aggregate_id);
