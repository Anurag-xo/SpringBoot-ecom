-- Add digital product capabilities to the products table
ALTER TABLE products ADD COLUMN is_digital BOOLEAN DEFAULT FALSE;
ALTER TABLE products ADD COLUMN digital_file_path VARCHAR(500);
