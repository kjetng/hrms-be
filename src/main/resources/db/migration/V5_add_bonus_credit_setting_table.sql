-- V1: Create bonus credit setting table
CREATE TABLE bonus_credit_setting (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,

                                      base_bonus_credits INT NOT NULL
                                          COMMENT 'Base bonus credits configured by admin',

                                      conversion_rate DOUBLE NOT NULL
        COMMENT 'Conversion rate for bonus credits',

                                      credit_date DATE NOT NULL
                                          COMMENT 'Date when bonus credits are applied'
);
