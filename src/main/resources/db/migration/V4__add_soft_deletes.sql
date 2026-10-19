-- Add soft delete columns to core tables
-- This preserves historical data, audit trails, and prevents foreign key constraint violations

ALTER TABLE products ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE product_variants ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE reviews ADD COLUMN deleted_at TIMESTAMP;
