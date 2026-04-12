-- ============================================================
-- Metrics Dashboard - Database Initialization Script
-- ============================================================

-- Create table
CREATE TABLE IF NOT EXISTS metrics (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100)   NOT NULL,
    metric_value DOUBLE PRECISION NOT NULL,
    status    VARCHAR(50)    NOT NULL,
    timestamp TIMESTAMP      NOT NULL
);

-- Index on timestamp for fast recent-metric queries
CREATE INDEX IF NOT EXISTS idx_metrics_timestamp ON metrics (timestamp DESC);

-- ── Seed sample data ──────────────────────────────────────────
INSERT INTO metrics (name, metric_value, status, timestamp) VALUES
    ('cpu_usage',        23.5,  'OK',       NOW() - INTERVAL '1 minute'),
    ('memory_usage',     67.8,  'WARNING',  NOW() - INTERVAL '2 minutes'),
    ('disk_io',          12.1,  'OK',       NOW() - INTERVAL '3 minutes'),
    ('network_latency',  88.4,  'CRITICAL', NOW() - INTERVAL '4 minutes'),
    ('request_rate',     54.0,  'WARNING',  NOW() - INTERVAL '5 minutes'),
    ('error_rate',        3.2,  'OK',       NOW() - INTERVAL '6 minutes'),
    ('response_time',    41.7,  'OK',       NOW() - INTERVAL '7 minutes'),
    ('throughput',       95.3,  'CRITICAL', NOW() - INTERVAL '8 minutes'),
    ('cache_hit_ratio',  78.6,  'WARNING',  NOW() - INTERVAL '9 minutes'),
    ('db_query_time',    15.9,  'OK',       NOW() - INTERVAL '10 minutes');
