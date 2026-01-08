-- Convert bonus_credit_setting.id to use IDENTITY (auto-increment)
-- This allows the column to auto-generate values when inserting

-- Create sequence for bonus_credit_setting id
CREATE SEQUENCE IF NOT EXISTS bonus_credit_setting_id_seq START WITH 1 INCREMENT BY 1;

-- Sync sequence with existing max ID to avoid duplicate key errors
SELECT setval('bonus_credit_setting_id_seq', COALESCE((SELECT MAX(id) FROM bonus_credit_setting), 0) + 1, false);

-- Set the default value for id to use the sequence
-- This works with JPA's GenerationType.IDENTITY
ALTER TABLE bonus_credit_setting
    ALTER COLUMN id SET DEFAULT nextval('bonus_credit_setting_id_seq');

-- Make the sequence owned by the column (for proper cleanup)
ALTER SEQUENCE bonus_credit_setting_id_seq OWNED BY bonus_credit_setting.id;

