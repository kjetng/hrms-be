ALTER TABLE employee
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE employee
    ADD status VARCHAR(255);

ALTER TABLE employee
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE redemption_transaction
    ALTER COLUMN amount_received TYPE DECIMAL USING (amount_received::DECIMAL);

ALTER TABLE position
    ALTER COLUMN salary TYPE DECIMAL USING (salary::DECIMAL);