CREATE TABLE bonus_credit_setting
(
    id BIGINT PRIMARY KEY,
    base_bonus_credits INT NOT NULL,
    conversion_rate INT NOT NULL,
    credit_date INT NOT NULL
);
