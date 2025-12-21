SET search_path = office;

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           1, 'USD',
           3, 'EUR',
           3, 0.97,
           1000.00, 970.00,
           'SELL', 'COMPLETED', 'Transaction completed successfully',
           NULL, NOW() - INTERVAL '5 days', NULL,
           0, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           4, 'EUR',
           1, 'USD',
           6, 1.05,
           500.00, 525.00,
           'BUY', 'COMPLETED', 'Transaction completed successfully',
           NULL, NOW() - INTERVAL '3 days', NULL,
           0, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           2, 'EUR',
           5, 'UAH',
           10, 44.30,
           1000.00, 44300.00,
           'SELL', 'PAUSED', 'Pending identity verification',
           NOW() - INTERVAL '1 day', NULL, NULL,
           0, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           6, 'EUR',
           1, 'USD',
           6, 1.05,
           2000.00, 2100.00,
           'BUY', 'CANCELLED', 'Customer changed mind',
           NULL, NULL, NOW() - INTERVAL '2 days',
           0, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           1, 'UAH',
           7, 'USD',
           8, 41.70,
           8340.00, 200.00,
           'SELL', 'COMPLETED', 'Transaction completed successfully',
           NULL, NOW() - INTERVAL '1 day', NULL,
           0, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           2, 'USD',
           8, 'EUR',
           3, 0.97,
           5000.00, 4850.00,
           'SELL', 'FAILED', 'Insufficient balance',
           NULL, NULL, NULL,
           0, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           9, 'USD',
           2, 'EUR',
           3, 0.94,
           3000.00, 2820.00,
           'BUY', 'COMPLETED', 'Transaction completed successfully',
           NULL, NOW() - INTERVAL '6 days', NULL,
           0, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           1, 'UAH',
           10, 'EUR',
           10, 44.30,
           22150.00, 500.00,
           'SELL', 'PAUSED', 'Waiting for rate confirmation',
           NOW() - INTERVAL '2 hours', NULL, NULL,
           0, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           3, 'EUR',
           2, 'USD',
           6, 1.05,
           1000.00, 1050.00,
           'BUY', 'COMPLETED', 'Transaction completed successfully',
           NULL, NOW() - INTERVAL '12 hours', NULL,
           0, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'
       );

INSERT INTO office.deals (
    seller_user_id, seller_currency,
    buyer_user_id, buyer_currency,
    day_rate_id, exchange_rate_used,
    sold_amount, purchased_amount,
    deal_type, status, status_reason,
    paused_at, completed_at, cancelled_at,
    version, created_at, updated_at
)
VALUES (
           1, 'USD',
           4, 'UAH',
           8, 41.70,
           2000.00, 83400.00,
           'SELL', 'CANCELLED', 'Better rate found elsewhere',
           NULL, NULL, NOW() - INTERVAL '8 hours',
           0, NOW() - INTERVAL '8 hours', NOW() - INTERVAL '8 hours'
       );