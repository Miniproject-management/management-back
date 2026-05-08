-- =========================
-- 스키마 생성
-- =========================

DROP TABLE IF EXISTS leave_balances;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;


CREATE TABLE departments (
    dept_no BIGINT NOT NULL AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    dept_desc VARCHAR(255),
    PRIMARY KEY (dept_no)
) ENGINE=InnoDB;


CREATE TABLE employees (
    emp_no BIGINT NOT NULL AUTO_INCREMENT,
    emp_name VARCHAR(100) NOT NULL,
    dept_no BIGINT NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    position VARCHAR(50) NOT NULL,
    hire_date DATE NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (emp_no)
) ENGINE=InnoDB;


CREATE TABLE leave_requests (
    leave_id BIGINT NOT NULL AUTO_INCREMENT,
    emp_no BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    leave_days DECIMAL(5,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    accrual_rule VARCHAR(255) NOT NULL,
    leave_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_MANAGER',
    approved_by BIGINT,
    is_active CHAR(1) NOT NULL DEFAULT 'Y',
    PRIMARY KEY (leave_id)
) ENGINE=InnoDB;


CREATE TABLE leave_balances (
    balance_id BIGINT NOT NULL AUTO_INCREMENT,
    emp_no BIGINT NOT NULL,
    year INT NOT NULL,
    total_leave DECIMAL(5,2) NOT NULL DEFAULT 15.00,
    used_leave DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (balance_id)
) ENGINE=InnoDB;


CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    action VARCHAR(50) NOT NULL,
    emp_no BIGINT,
    target_id BIGINT,
    detail VARCHAR(500),
    ip_address VARCHAR(45),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;


ALTER TABLE employees
ADD CONSTRAINT FK_employees_department
FOREIGN KEY (dept_no)
REFERENCES departments(dept_no);


ALTER TABLE leave_requests
ADD CONSTRAINT FK_leave_employee
FOREIGN KEY (emp_no)
REFERENCES employees(emp_no);


ALTER TABLE leave_requests
ADD CONSTRAINT FK_leave_approver
FOREIGN KEY (approved_by)
REFERENCES employees(emp_no);


ALTER TABLE leave_balances
ADD CONSTRAINT FK_balance_employee
FOREIGN KEY (emp_no)
REFERENCES employees(emp_no);
