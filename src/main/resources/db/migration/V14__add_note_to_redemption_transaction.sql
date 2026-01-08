-- Add note field to redemption_transaction table
ALTER TABLE redemption_transaction
    ADD COLUMN note VARCHAR(500);
