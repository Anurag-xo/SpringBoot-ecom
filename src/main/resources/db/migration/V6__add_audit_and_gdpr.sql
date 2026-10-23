-- V6__add_audit_and_gdpr.sql
-- Add Audit Tables for GDPR & Security Compliance

-- 1. Orders Audit Table
CREATE TABLE orders_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    customer_name VARCHAR(255),
    customer_email VARCHAR(255),
    status VARCHAR(255),
    total_price NUMERIC(10, 2),
    coupon_code VARCHAR(50),
    discount_amount NUMERIC(10, 2),
    user_id BIGINT,
    created_at TIMESTAMP,
    deleted_at TIMESTAMP,
    PRIMARY KEY (id, rev)
);

-- 2. Order Items Audit Table
CREATE TABLE order_items_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    order_id BIGINT,
    product_id BIGINT,
    variant_id BIGINT,
    quantity INTEGER,
    price_at_purchase NUMERIC(10, 2),
    PRIMARY KEY (id, rev)
);

-- 3. Coupons Audit Table
CREATE TABLE coupons_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    code VARCHAR(50),
    discount_type VARCHAR(20),
    discount_value NUMERIC(10, 2),
    min_order_amount NUMERIC(10, 2),
    max_discount_amount NUMERIC(10, 2),
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    usage_limit INTEGER,
    times_used INTEGER,
    is_active BOOLEAN,
    PRIMARY KEY (id, rev)
);

-- 4. Users Audit Table
CREATE TABLE users_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    username VARCHAR(255),
    email VARCHAR(255),
    enabled BOOLEAN,
    created_at TIMESTAMP,
    deleted_at TIMESTAMP,
    PRIMARY KEY (id, rev)
);
