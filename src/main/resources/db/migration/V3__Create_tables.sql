CREATE EXTENSION IF NOT EXISTS pgcrypto SCHEMA office;

CREATE TABLE office.users (
                              id SERIAL PRIMARY KEY,

                              username VARCHAR(100) NOT NULL UNIQUE,
                              password VARCHAR(100) NOT NULL,

                              role VARCHAR(20) NOT NULL
                                  CHECK (role IN ('ADMIN', 'CUSTOMER')),

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_role ON office.users (role);
CREATE INDEX idx_user_created ON office.users (created_at);

CREATE TABLE office.day_rates (
                                  id SERIAL PRIMARY KEY,
                                  base_currency VARCHAR(20) NOT NULL,
                                  quote_currency VARCHAR(20) NOT NULL,
                                  rate_date DATE NOT NULL,
                                  buy_rate NUMERIC(19, 6) NOT NULL,
                                  sell_rate NUMERIC(19, 6) NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  CONSTRAINT uq_day_rate_currency_date UNIQUE (base_currency, quote_currency, rate_date)
);

CREATE INDEX idx_rate_date ON office.day_rates(rate_date);

CREATE TABLE office.currency_balances (
                                          id SERIAL PRIMARY KEY,
                                          user_id INTEGER NOT NULL,
                                          currency VARCHAR(20) NOT NULL,
                                          amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
                                          version BIGINT NOT NULL DEFAULT 0,
                                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                          updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                          CONSTRAINT fk_balance_user FOREIGN KEY (user_id) REFERENCES office.users(id) ON DELETE CASCADE,
                                          CONSTRAINT uq_user_currency UNIQUE (user_id, currency)
);

CREATE INDEX idx_balance_currency ON office.currency_balances(currency);

CREATE TABLE office.deals (
                              id SERIAL PRIMARY KEY,
                              seller_user_id INTEGER NOT NULL,
                              seller_currency VARCHAR(20) NOT NULL,
                              buyer_user_id INTEGER NOT NULL,
                              buyer_currency VARCHAR(20) NOT NULL,
                              day_rate_id INTEGER NOT NULL,
                              exchange_rate_used NUMERIC(19, 6),
                              sold_amount NUMERIC(19, 4) NOT NULL,
                              purchased_amount NUMERIC(19, 4),
                              deal_type VARCHAR(10) NOT NULL CHECK (deal_type IN ('BUY', 'SELL')),
                              status VARCHAR(20) NOT NULL CHECK (status IN ('PAUSED', 'COMPLETED', 'FAILED', 'CANCELLED')),
                              status_reason VARCHAR(200),
                              paused_at TIMESTAMP,
                              completed_at TIMESTAMP,
                              cancelled_at TIMESTAMP,
                              version BIGINT NOT NULL DEFAULT 0,
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              CONSTRAINT fk_deal_seller FOREIGN KEY (seller_user_id) REFERENCES office.users(id),
                              CONSTRAINT fk_deal_buyer FOREIGN KEY (buyer_user_id) REFERENCES office.users(id),
                              CONSTRAINT fk_deal_rate FOREIGN KEY (day_rate_id) REFERENCES office.day_rates(id)
);

CREATE INDEX idx_deal_created_at ON office.deals(created_at);
CREATE INDEX idx_deal_seller_id ON office.deals(seller_user_id);
CREATE INDEX idx_deal_buyer_id ON office.deals(buyer_user_id);
CREATE INDEX idx_deal_type_created ON office.deals(deal_type, created_at);
CREATE INDEX idx_deal_status_created ON office.deals(status, created_at);