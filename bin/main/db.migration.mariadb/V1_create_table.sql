DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;


-- 부서정보
CREATE TABLE departments (
    dept_no BIGINT NOT NULL AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    dept_desc VARCHAR(255),

    PRIMARY KEY (dept_no)
) ENGINE=InnoDB;


-- 인사정보
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


-- 연차정보
CREATE TABLE leave_requests (
    leave_id BIGINT NOT NULL AUTO_INCREMENT,
    emp_no BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    leave_days DECIMAL(5,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    accrual_rule VARCHAR(255) NOT NULL,

     -- 결재 상태
        -- PENDING_MANAGER : 팀장 승인 대기
        -- PENDING_HR      : 인사팀 승인 대기
        -- APPROVED        : 최종 승인
        -- REJECTED        : 반려
        -- CANCELED        : 신청 취소
    leave_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_MANAGER',
    -- 마지막 승인자
        approved_by BIGINT,

    is_active CHAR(1) NOT NULL DEFAULT 'Y',

    PRIMARY KEY (leave_id)
) ENGINE=InnoDB;



-- FK 설정
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