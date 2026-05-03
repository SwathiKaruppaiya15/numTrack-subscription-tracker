-- ============================================================
-- SubTrack — PostgreSQL Schema
-- ============================================================

-- ENUMS
CREATE TYPE IF NOT EXISTS billing_cycle AS ENUM ('MONTHLY', 'YEARLY');
CREATE TYPE IF NOT EXISTS payment_status AS ENUM ('SUCCESS', 'FAILED', 'PENDING');
CREATE TYPE IF NOT EXISTS subscription_status AS ENUM ('ACTIVE', 'CANCELLED', 'AT_RISK');
CREATE TYPE IF NOT EXISTS reminder_type AS ENUM ('THREE_DAYS', 'ONE_DAY');

-- ── users ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    timezone      VARCHAR(50)  NOT NULL DEFAULT 'Asia/Kolkata',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── subscriptions ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS subscriptions (
    id                UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name              VARCHAR(100)      NOT NULL,
    category          VARCHAR(50)       NOT NULL,
    amount            NUMERIC(12, 2)    NOT NULL CHECK (amount > 0),
    currency          VARCHAR(5)        NOT NULL DEFAULT 'INR',
    billing_cycle     billing_cycle     NOT NULL,
    start_date        DATE              NOT NULL,
    next_billing_date DATE              NOT NULL,
    status            subscription_status NOT NULL DEFAULT 'ACTIVE',
    notes             TEXT,
    created_at        TIMESTAMP         NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP         NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_subscription UNIQUE (user_id, name)
);

-- ── payments ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
    id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID           NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    user_id         UUID           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount          NUMERIC(12, 2) NOT NULL,
    currency        VARCHAR(5)     NOT NULL DEFAULT 'INR',
    status          payment_status NOT NULL,
    failure_reason  TEXT,
    payment_date    TIMESTAMP      NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── usage_logs ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usage_logs (
    id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID      NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    user_id         UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    used_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── reminders ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reminders (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID          NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reminder_type   reminder_type NOT NULL,
    scheduled_for   DATE          NOT NULL,
    sent_at         TIMESTAMP,
    email_sent_to   VARCHAR(255),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reminder UNIQUE (subscription_id, reminder_type, scheduled_for)
);

-- ── INDEXES ────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id         ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_next_billing    ON subscriptions(next_billing_date);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status          ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_payments_user_id              ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_subscription_id      ON payments(subscription_id);
CREATE INDEX IF NOT EXISTS idx_payments_date                 ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_usage_logs_subscription_id    ON usage_logs(subscription_id);
CREATE INDEX IF NOT EXISTS idx_usage_logs_used_at            ON usage_logs(used_at);
CREATE INDEX IF NOT EXISTS idx_reminders_subscription_id     ON reminders(subscription_id);
CREATE INDEX IF NOT EXISTS idx_reminders_sent_at             ON reminders(sent_at);
CREATE INDEX IF NOT EXISTS idx_reminders_scheduled_for       ON reminders(scheduled_for);
