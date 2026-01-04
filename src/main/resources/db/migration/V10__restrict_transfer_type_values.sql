ALTER TABLE transfer_transaction
    ADD CONSTRAINT chk_transfer_type_valid_values
    CHECK (transfer_type IN ('REDEEM','TRANSFER','DEDUCT','AWARD','MONTHLY'));