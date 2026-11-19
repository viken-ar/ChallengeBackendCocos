CREATE TABLE idempotency_keys
(
    "key"         UUID PRIMARY KEY,
    user_id       INT         NOT NULL,
    request_hash  VARCHAR(64) NOT NULL,
    response_body TEXT,
    status_code   INT         NOT NULL,
    "status"      VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_orders_user_status ON orders (userId, status);
CREATE INDEX idx_orders_user_instrument ON orders (userId, instrumentId);
CREATE INDEX idx_marketdata_instrument_date ON marketdata (instrumentId, "date" DESC);
CREATE INDEX idx_instruments_ticker ON instruments (ticker);
CREATE INDEX idx_instruments_name ON instruments ("name");
