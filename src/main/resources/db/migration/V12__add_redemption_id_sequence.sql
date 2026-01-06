-- Create sequence for redemption_transaction id
CREATE SEQUENCE IF NOT EXISTS redemption_transaction_seq START WITH 1 INCREMENT BY 1;

-- Sync sequence with existing max ID to avoid duplicate key errors
SELECT setval('redemption_transaction_seq', COALESCE((SELECT MAX(redemption_id) FROM redemption_transaction), 0) + 1, false);

-- Set the default value for redemption_id to use the sequence
ALTER TABLE redemption_transaction
    ALTER COLUMN redemption_id SET DEFAULT nextval('redemption_transaction_seq');
