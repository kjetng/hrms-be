ALTER TABLE transfer_transaction
ADD COLUMN IF NOT EXISTS transfer_type VARCHAR(50);
