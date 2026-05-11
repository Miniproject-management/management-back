-- =========================
-- 부서정보 샘플 데이터
-- =========================
INSERT INTO departments (dept_name, dept_desc) VALUES
('인사팀', '인사 및 조직 관리'),
('개발팀', '서비스 개발 및 운영'),
('디자인팀', 'UI/UX 디자인');


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
('루키즈', 2, 'Backend Developer', '사원', '2024-01-02', 'password123'),
('김팀장', 2, 'Development Team Leader', '팀장', '2023-05-15', 'password123'),
('박인사', 1, 'HR Manager', '인사담당자', '2022-03-01', 'password123');


-- =========================
-- 연차 신청 샘플 데이터
-- =========================
INSERT INTO leave_requests (
    emp_no,
    leave_type,
    leave_days,
    start_date,
    end_date,
    reason,
    accrual_rule,
    leave_status,
    approved_by,
    is_active
) VALUES

-- =========================
-- 연차 잔여량 샘플 데이터
-- =========================
INSERT INTO leave_balances (emp_no, year, total_leave, used_leave) VALUES
(1, 2026, 15.00, 3.50),
(2, 2026, 15.00, 1.00),
(3, 2026, 15.00, 2.00);


-- 팀장 승인 대기
(
    1,
    '연차',
    1,
    '2026-05-10',
    '2026-05-10',
    '개인 일정',
    '매년 1월 1일 기준',
    'PENDING_MANAGER',
    NULL,
    'Y'
),

-- 인사팀 승인 대기
(
    1,
    '오전반차',
    0.5,
    '2026-05-15',
    '2026-05-15',
    '병원 방문',
    '매년 1월 1일 기준',
    'PENDING_HR',
    2,
    'Y'
),

-- 최종 승인 완료
(
    1,
    '연차',
    2,
    '2026-04-20',
    '2026-04-21',
    '여행',
    '매년 1월 1일 기준',
    'APPROVED',
    3,
    'Y'
),

-- 반려
(
    1,
    '연차',
    1,
    '2026-03-10',
    '2026-03-10',
    '개인 업무',
    '매년 1월 1일 기준',
    'REJECTED',
    2,
    'Y'
);