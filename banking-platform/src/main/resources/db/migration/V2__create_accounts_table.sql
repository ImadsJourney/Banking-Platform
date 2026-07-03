CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    iban VARCHAR(34) NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
    daily_limit DECIMAL(19, 2),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
