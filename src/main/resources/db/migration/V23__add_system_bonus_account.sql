-- Add system bonus point account with empId = -1
-- This account is used for system operations like monthly credit distribution

INSERT INTO bonus_point_account (emp_id, bonus_point)
VALUES (-1, 0)
ON CONFLICT (emp_id) DO NOTHING;
