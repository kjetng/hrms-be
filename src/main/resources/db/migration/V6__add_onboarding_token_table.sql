-- Create onboarding_token table for storing secure form access tokens
CREATE TABLE onboarding_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    employee_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_onboarding_token_employee FOREIGN KEY (employee_id) REFERENCES employee(emp_id) ON DELETE CASCADE
);

-- Create index for faster token lookups
CREATE INDEX idx_onboarding_token_token ON onboarding_token(token);
CREATE INDEX idx_onboarding_token_employee_id ON onboarding_token(employee_id);

