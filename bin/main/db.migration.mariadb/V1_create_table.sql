-- =========================
-- 기존 데이터 삭제
-- =========================

DELETE FROM leave_requests;
DELETE FROM employees;
DELETE FROM departments;


-- AUTO_INCREMENT 초기화
ALTER TABLE departments AUTO_INCREMENT = 1;
ALTER TABLE employees AUTO_INCREMENT = 1;
ALTER TABLE leave_requests AUTO_INCREMENT = 1;


-- =========================
-- 부서 데이터
-- =========================

INSERT INTO departments (
    dept_name,
    dept_desc
) VALUES

('인사팀', '인사 및 조직 관리'),
('개발팀', '서비스 개발 및 운영'),
('디자인팀', 'UI/UX 디자인');


-- =========================
-- 직원 데이터
-- =========================

INSERT INTO employees (
    emp_name,
    dept_no,
    job_title,
    position,
    hire_date,
    password
) VALUES

-- 일반 사원
(
    '루키즈',
    2,
    'Backend Developer',
    '사원',
    '2024-01-02',
    'password123'
),

-- 개발팀 팀장
(
    '김팀장',
    2,
    'Development Team Leader',
    '팀장',
    '2023-05-15',
    'password123'
),

-- 인사팀 관리자
(
    '박인사',
    1,
    'HR Manager',
    '팀장',
    '2022-03-01',
    'password123'
),

-- 디자인팀 사원
(
    '최디자인',
    3,
    'UI Designer',
    '사원',
    '2024-07-01',
    'password123'
);


-- =========================
-- 연차 신청 데이터
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


-- 팀장 승인 대기
(
    1,
    '연차',
    1.0,
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
    2.0,
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
    4,
    '연차',
    1.0,
    '2026-03-10',
    '2026-03-10',
    '개인 업무',
    '매년 1월 1일 기준',
    'REJECTED',
    2,
    'Y'
);