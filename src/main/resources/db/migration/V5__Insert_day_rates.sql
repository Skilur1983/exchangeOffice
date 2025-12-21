SET search_path = office;

INSERT INTO office.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('USD', 'EUR', '2024-12-01', 0.92, 0.95, NOW(), NOW()),
    ('USD', 'EUR', '2024-12-10', 0.93, 0.96, NOW(), NOW()),
    ('USD', 'EUR', CURRENT_DATE, 0.94, 0.97, NOW(), NOW());

INSERT INTO office.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('EUR', 'USD', '2024-12-01', 1.05, 1.09, NOW(), NOW()),
    ('EUR', 'USD', '2024-12-10', 1.04, 1.08, NOW(), NOW()),
    ('EUR', 'USD', CURRENT_DATE, 1.03, 1.06, NOW(), NOW());

INSERT INTO office.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('USD', 'UAH', '2024-12-01', 41.00, 41.50, NOW(), NOW()),
    ('USD', 'UAH', CURRENT_DATE, 41.20, 41.70, NOW(), NOW());

INSERT INTO office.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('EUR', 'UAH', '2024-12-01', 43.50, 44.00, NOW(), NOW()),
    ('EUR', 'UAH', CURRENT_DATE, 43.80, 44.30, NOW(), NOW());