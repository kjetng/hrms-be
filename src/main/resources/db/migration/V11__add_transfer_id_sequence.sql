-- Create sequence for transfer_transaction id
CREATE SEQUENCE IF NOT EXISTS transfer_transaction_seq START WITH 1 INCREMENT BY 1;

-- Sync sequence with existing max ID to avoid duplicate key errors
SELECT setval('transfer_transaction_seq', COALESCE((SELECT MAX(transfer_id) FROM transfer_transaction), 0) + 1, false);

-- Set the default value for transfer_id to use the sequence
ALTER TABLE transfer_transaction
    ALTER COLUMN transfer_id SET DEFAULT nextval('transfer_transaction_seq');
