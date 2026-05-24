CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    actor_email VARCHAR(255) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   VARCHAR(100),
    detail      TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor      ON audit_logs (actor_email);
CREATE INDEX idx_audit_action     ON audit_logs (action);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at DESC);
