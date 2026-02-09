SET search_path = office;

INSERT INTO office_test.users (username, password, role)
VALUES
    ('admin_first', crypt('admin123password', gen_salt('bf')), 'ADMIN'),
    ('admin_second', crypt('admin123password', gen_salt('bf')), 'ADMIN');

INSERT INTO office_test.users (username, password, role)
VALUES
    ('SarSmi', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('TomBro', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('JohDoe', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('MarJon', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('AnnWil', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('PetBak', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('LisChe', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('DavMil', crypt('customer123', gen_salt('bf')), 'CUSTOMER');

INSERT INTO office_test.users (username, password, role)
VALUES
    ('admin_third', crypt('admin123password', gen_salt('bf')), 'ADMIN');

INSERT INTO office_test.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('USD', 'EUR', '2024-12-01', 0.92, 0.95, NOW(), NOW()),
    ('USD', 'EUR', '2024-12-10', 0.93, 0.96, NOW(), NOW()),
    ('USD', 'EUR', CURRENT_DATE, 0.94, 0.97, NOW(), NOW());

INSERT INTO office_test.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('EUR', 'USD', '2024-12-01', 1.05, 1.09, NOW(), NOW()),
    ('EUR', 'USD', '2024-12-10', 1.04, 1.08, NOW(), NOW()),
    ('EUR', 'USD', CURRENT_DATE, 1.03, 1.06, NOW(), NOW());

INSERT INTO office_test.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('USD', 'UAH', '2024-12-01', 41.00, 41.50, NOW(), NOW()),
    ('USD', 'UAH', CURRENT_DATE, 41.20, 41.70, NOW(), NOW());

INSERT INTO office_test.day_rates (base_currency, quote_currency, rate_date, buy_rate, sell_rate, created_at, updated_at)
VALUES
    ('EUR', 'UAH', '2024-12-01', 43.50, 44.00, NOW(), NOW()),
    ('EUR', 'UAH', CURRENT_DATE, 43.80, 44.30, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (1, 'USD', 100000.00, 0, NOW(), NOW()),
    (1, 'EUR', 100000.00, 0, NOW(), NOW()),
    (1, 'UAH', 4000000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (3, 'USD', 5000.00, 0, NOW(), NOW()),
    (3, 'EUR', 3000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (4, 'USD', 2500.00, 0, NOW(), NOW()),
    (4, 'EUR', 4000.00, 0, NOW(), NOW()),
    (4, 'UAH', 100000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (5, 'USD', 1000.00, 0, NOW(), NOW()),
    (5, 'UAH', 50000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (6, 'EUR', 10000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (7, 'USD', 750.50, 0, NOW(), NOW()),
    (7, 'UAH', 25000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (8, 'USD', 100.00, 0, NOW(), NOW()),
    (8, 'EUR', 150.00, 0, NOW(), NOW()),
    (8, 'UAH', 5000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (9, 'USD', 15000.00, 0, NOW(), NOW());

INSERT INTO office_test.currency_balances (user_id, currency, amount, version, created_at, updated_at)
VALUES
    (10, 'EUR', 2000.00, 0, NOW(), NOW()),
    (10, 'UAH', 75000.00, 0, NOW(), NOW());

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status,
    completed_at,
    created_at, updated_at
) VALUES (
             1, 'USD',
             3, 'EUR',
             3,
             0.97,
             1000.00, 970.00,
             'SELL', 'COMPLETED',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status,
    completed_at,
    created_at, updated_at
) VALUES (
             4, 'EUR',
             1, 'USD',
             6,
             1.03,
             500.00, 515.00,
             'BUY', 'COMPLETED',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at,
    created_at, updated_at
) VALUES (
             1, 'UAH',
             6, 'EUR',
             10,
             44.30,
             50000.00, 1128.67,
             'SELL', 'PAUSED', 'Buyer has insufficient balance',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status,
    completed_at,
    created_at, updated_at
) VALUES (
             5, 'UAH',
             1, 'USD',
             8,
             41.20,
             20000.00, 485.44,
             'BUY', 'COMPLETED',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    cancelled_at,
    created_at, updated_at
) VALUES (
             1, 'EUR',
             7, 'USD',
             6,
             1.06,
             300.00, 318.00,
             'SELL', 'CANCELLED', 'Cancelled by admin',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status,
    completed_at,
    created_at, updated_at
) VALUES (
             1, 'USD',
             4, 'UAH',
             8,
             41.70,
             100.00, 4170.00,
             'SELL', 'COMPLETED',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at,
    created_at, updated_at
) VALUES (
             3, 'USD',
             1, 'EUR',
             3,
             0.94,
             2000.00, 1880.00,
             'BUY', 'PAUSED', 'Awaiting confirmation',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status,
    completed_at,
    created_at, updated_at
) VALUES (
             1, 'EUR',
             8, 'UAH',
             10,
             44.30,
             50.00, 2215.00,
             'SELL', 'COMPLETED',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status,
    completed_at,
    created_at, updated_at
) VALUES (
             9, 'USD',
             1, 'EUR',
             3,
             0.94,
             1000.00, 940.00,
             'BUY', 'COMPLETED',
             NOW(),
             NOW(), NOW()
         );

INSERT INTO office_test.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    cancelled_at,
    created_at, updated_at
) VALUES (
             1, 'UAH',
             10, 'EUR',
             10,
             43.80,
             10000.00, 228.31,
             'SELL', 'CANCELLED', 'Customer request',
             NOW(),
             NOW(), NOW()
         );