-- ============================================================
-- Subscription Service - Database Initialization Script
-- Tables: subscriptions, msisdn_pool, sim_cards, outbox_events
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- SUBSCRIPTIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID NOT NULL,
    msisdn          CHARACTER VARYING(20) NOT NULL,
    tariff_code     CHARACTER VARYING(50) NOT NULL,
    status          CHARACTER VARYING(20) NOT NULL DEFAULT 'ACTIVE',
    activated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    terminated_at   TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_customer_id ON subscriptions(customer_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_msisdn      ON subscriptions(msisdn);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status      ON subscriptions(status);

-- ============================================================
-- MSISDN POOL
-- ============================================================
CREATE TABLE IF NOT EXISTS msisdn_pool (
    msisdn          CHARACTER VARYING(20) PRIMARY KEY,
    status          CHARACTER VARYING(20) NOT NULL DEFAULT 'FREE',
    reserved_until  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_msisdn_pool_status ON msisdn_pool(status);

-- Seed pool with 20 sample MSISDNs (Turkcell 05xx range)
INSERT INTO msisdn_pool (msisdn, status) VALUES
    ('05301000001', 'FREE'),
    ('05301000002', 'FREE'),
    ('05301000003', 'FREE'),
    ('05301000004', 'FREE'),
    ('05301000005', 'FREE'),
    ('05301000006', 'FREE'),
    ('05301000007', 'FREE'),
    ('05301000008', 'FREE'),
    ('05301000009', 'FREE'),
    ('05301000010', 'FREE'),
    ('05301000011', 'FREE'),
    ('05301000012', 'FREE'),
    ('05301000013', 'FREE'),
    ('05301000014', 'FREE'),
    ('05301000015', 'FREE'),
    ('05301000016', 'FREE'),
    ('05301000017', 'FREE'),
    ('05301000018', 'FREE'),
    ('05301000019', 'FREE'),
    ('05301000020', 'FREE')
ON CONFLICT (msisdn) DO NOTHING;

-- ============================================================
-- SIM CARDS
-- ============================================================
CREATE TABLE IF NOT EXISTS sim_cards (
    iccid   CHARACTER VARYING(30) PRIMARY KEY,
    imsi    CHARACTER VARYING(20) NOT NULL,
    msisdn  CHARACTER VARYING(20) NOT NULL,
    status  CHARACTER VARYING(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX IF NOT EXISTS idx_sim_cards_msisdn ON sim_cards(msisdn);

-- ============================================================
-- OUTBOX EVENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS outbox_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  CHARACTER VARYING(100) NOT NULL,
    aggregate_id    CHARACTER VARYING(100) NOT NULL,
    type            CHARACTER VARYING(100) NOT NULL,
    payload         JSONB NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMP WITH TIME ZONE,
    status          CHARACTER VARYING(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status       ON outbox_events(status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate_id ON outbox_events(aggregate_id);
