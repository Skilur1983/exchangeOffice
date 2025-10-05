CREATE EXTENSION IF NOT EXISTS pgcrypto SCHEMA office;

CREATE TABLE office.users (
                                  id SERIAL PRIMARY KEY,
                                  username VARCHAR(255) NOT NULL UNIQUE,
                                  password VARCHAR(255) NOT NULL,
                                  role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('ADMIN', 'CUSTOMER')));