-- =========================
-- 부서정보 샘플 데이터
-- =========================
INSERT INTO departments (dept_name, dept_desc) VALUES
('HR', 'performs human resource management functions'),
('Marketing', 'creates strategies for selling company products'),
('Sales', 'identifies sales goals and prepares sales plans');



-- =========================
-- 인사정보 샘플 데이터
-- =========================
INSERT INTO employees (
    emp_name,
    dept_no,
    job_title,
    position,
    hire_date,
    password
) VALUES
('John Smith', 1, 'HR Manager', 'Manager', '2024-01-02', 'password123'),
('Sarah Johnson', 2, 'Marketing Specialist', 'Staff', '2024-02-15', 'password123'),
('Emily Brown', 3, 'Sales Representative', 'Staff', '2024-03-01', 'password123');



-- =========================
-- 연차정보 샘플 데이터
-- =========================
INSERT INTO leave_type (
    emp_no,
    leave_type,
    leave_days,
    accrual_rule,
    leave_status,
    is_active
) VALUES
(1, '연차', 15, '매년 1월 1일 기준', 'APPROVED', 'Y'),
(1, '경조사', 3, '발생 시 부여', 'PENDING', 'Y'),

(2, '연차', 12, '매년 1월 1일 기준', 'REJECTED', 'Y'),
(2, '병가', 10, '매년 1월 1일 기준', 'APPROVED', 'Y'),

(3, '연차', 13, '매년 1월 1일 기준', 'PENDING', 'Y');