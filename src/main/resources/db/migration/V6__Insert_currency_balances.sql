SET search_path = office;

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (1, 'USD', 150000.00, 0, NOW(), NOW()),
    (1, 'EUR', 140000.00, 0, NOW(), NOW()),
    (1, 'UAH', 1000000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (2, 'USD', 200000.00, 0, NOW(), NOW()),
    (2, 'EUR', 300000.00, 0, NOW(), NOW()),
    (2, 'UAH', 1000000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (3, 'USD', 5000.00, 0, NOW(), NOW()),
    (3, 'EUR', 3000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (4, 'USD', 2500.00, 0, NOW(), NOW()),
    (4, 'EUR', 4000.00, 0, NOW(), NOW()),
    (4, 'UAH', 100000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (5, 'USD', 1000.00, 0, NOW(), NOW()),
    (5, 'UAH', 50000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (6, 'EUR', 10000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (7, 'USD', 750.50, 0, NOW(), NOW()),
    (7, 'UAH', 25000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (8, 'USD', 100.00, 0, NOW(), NOW()),
    (8, 'EUR', 150.00, 0, NOW(), NOW()),
    (8, 'UAH', 5000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (9, 'USD', 15000.00, 0, NOW(), NOW());

INSERT INTO office.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (10, 'EUR', 2000.00, 0, NOW(), NOW()),
    (10, 'UAH', 75000.00, 0, NOW(), NOW());