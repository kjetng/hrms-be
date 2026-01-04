ALTER TABLE app_user
    DROP CONSTRAINT fk_app_user_on_emp;

ALTER TABLE bank_account
    DROP CONSTRAINT fk_bank_account_on_emp;

ALTER TABLE bonus_point_account
    DROP CONSTRAINT fk_bonus_point_account_on_emp;

ALTER TABLE campaign_participant
    DROP CONSTRAINT fk_campaign_participant_on_emp;

ALTER TABLE department
    DROP CONSTRAINT fk_department_on_manager;

ALTER TABLE education
    DROP CONSTRAINT fk_education_on_emp;

ALTER TABLE employee_activity
    DROP CONSTRAINT fk_employee_activity_on_emp;

ALTER TABLE employee
    DROP CONSTRAINT fk_employee_on_dept;

ALTER TABLE employee
    DROP CONSTRAINT fk_employee_on_manager;

ALTER TABLE employee
    DROP CONSTRAINT fk_employee_on_position;

ALTER TABLE request
    DROP CONSTRAINT fk_request_on_approver;

ALTER TABLE request
    DROP CONSTRAINT fk_request_on_requester;

DROP TABLE bank_account CASCADE;

DROP TABLE department CASCADE;

DROP TABLE education CASCADE;

DROP TABLE employee CASCADE;

DROP TABLE position CASCADE;

DROP TABLE request CASCADE;

ALTER TABLE redemption_transaction
    ALTER COLUMN amount_received TYPE DECIMAL USING (amount_received::DECIMAL);