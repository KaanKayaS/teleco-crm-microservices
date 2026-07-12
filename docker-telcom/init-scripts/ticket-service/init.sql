-- ============================================================
-- Ticket Service — Database Initialization Script
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------
-- tickets
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id  UUID         NOT NULL,
    category     VARCHAR(50)  NOT NULL,      -- COMPLAINT | REQUEST | FAULT
    priority     VARCHAR(20)  NOT NULL,      -- LOW | MEDIUM | HIGH | CRITICAL
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN', -- OPEN | IN_PROGRESS | RESOLVED | CLOSED
    description  TEXT,
    assigned_to  UUID,                       -- Agent/staff UUID (nullable)
    sla_due_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at  TIMESTAMP WITH TIME ZONE,
    sla_breached BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_tickets_customer_id  ON tickets (customer_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status       ON tickets (status);
CREATE INDEX IF NOT EXISTS idx_tickets_sla_due_at   ON tickets (sla_due_at);

-- ------------------------------------------------------------
-- ticket_comments
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ticket_comments (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id  UUID        NOT NULL REFERENCES tickets (id) ON DELETE CASCADE,
    author_id  UUID        NOT NULL,
    body       TEXT        NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ticket_comments_ticket_id ON ticket_comments (ticket_id);

-- ------------------------------------------------------------
-- outbox_events
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS outbox_events (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(100),
    aggregate_id   VARCHAR(100),
    type           VARCHAR(100),
    payload        JSONB,
    created_at     TIMESTAMP WITH TIME ZONE,
    processed_at   TIMESTAMP WITH TIME ZONE,
    status         VARCHAR(20)                  -- PENDING | PROCESSED | FAILED
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON outbox_events (status);
