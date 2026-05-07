# 팀 역할 분담 및 작업 파일 가이드

## 최혜인 — 인증 / 결재 담당

### 담당 도메인
- `domain/auth/` — 로그인, JWT, 권한 관리
- `domain/approval/` — 전자결재 (연차/반차 신청, 승인/반려)

### 작업할 파일 목록

```
# auth 도메인
domain/auth/entity/Member.java              # 사용자 엔티티 (이메일, 비밀번호, Role)
domain/auth/repository/MemberRepository.java
domain/auth/service/AuthService.java         # 로그인, 회원가입
domain/auth/controller/AuthController.java   # POST /api/auth/login, /signup
domain/auth/dto/LoginRequest.java
domain/auth/dto/LoginResponse.java
domain/auth/dto/SignupRequest.java

# approval 도메인
domain/approval/entity/Approval.java         # 결재 엔티티 (신청자, 승인자, 상태, 타입)
domain/approval/repository/ApprovalRepository.java
domain/approval/service/ApprovalService.java # 결재 신청, 승인, 반려
domain/approval/controller/ApprovalController.java
domain/approval/dto/ApprovalRequest.java
domain/approval/dto/ApprovalResponse.java

# global (공통 - 팀 협의 후 작업)
global/security/JwtTokenProvider.java        # JWT 토큰 생성/검증
global/security/JwtAuthenticationFilter.java # 필터
global/config/SecurityConfig.java            # Spring Security 설정
```

### 프론트 화면
- 로그인 화면
- 결재 신청 화면
- 승인 목록 화면

---

## 윤형진 — HR / 조직도 담당

### 담당 도메인
- `domain/employee/` — 직원 CRUD
- `domain/department/` — 부서 관리, 조직도

### 작업할 파일 목록

```
# employee 도메인
domain/employee/entity/Employee.java         # 직원 엔티티 (이름, 직급, 부서, 입사일 등)
domain/employee/repository/EmployeeRepository.java
domain/employee/service/EmployeeService.java # 직원 CRUD, 검색
domain/employee/controller/EmployeeController.java
domain/employee/dto/EmployeeRequest.java
domain/employee/dto/EmployeeResponse.java

# department 도메인
domain/department/entity/Department.java     # 부서 엔티티 (부서명, 상위부서)
domain/department/repository/DepartmentRepository.java
domain/department/service/DepartmentService.java # 부서 CRUD, 트리 조회
domain/department/controller/DepartmentController.java
domain/department/dto/DepartmentRequest.java
domain/department/dto/DepartmentResponse.java
```

### 프론트 화면
- 조직도 UI (트리 구조)
- 직원 목록 / 상세 화면
- 부서 관리 화면

---

## 심소담 — AI / ATS 담당

### 담당 도메인
- `domain/ai/` — AI 자소서 분석
- `domain/ats/` — 지원자 추적 시스템

### 작업할 파일 목록

```
# ai 도메인 (repository/entity 없음 — 외부 API 호출 위주)
domain/ai/service/AiAnalysisService.java     # 자소서 분석 (요약, 키워드, 적합도)
domain/ai/service/PromptGuardService.java    # Prompt Injection 방어
domain/ai/controller/AiController.java       # POST /api/ai/analyze
domain/ai/dto/AnalysisRequest.java
domain/ai/dto/AnalysisResponse.java

# ats 도메인
domain/ats/entity/Applicant.java             # 지원자 엔티티
domain/ats/entity/Resume.java                # 자소서 엔티티
domain/ats/repository/ApplicantRepository.java
domain/ats/repository/ResumeRepository.java
domain/ats/service/AtsService.java           # 지원자 관리, 자소서 저장
domain/ats/controller/AtsController.java
domain/ats/dto/ApplicantRequest.java
domain/ats/dto/ApplicantResponse.java
domain/ats/dto/ResumeRequest.java
domain/ats/dto/ResumeResponse.java
```

### 프론트 화면
- 자소서 업로드 화면
- AI 분석 결과 UI
- 지원자 목록 화면

---

## 김민희 — 근태 / 연차 담당

### 담당 도메인
- `domain/attendance/` — 출퇴근, 근무시간
- `domain/leave/` — 연차 관리

### 작업할 파일 목록

```
# attendance 도메인
domain/attendance/entity/Attendance.java     # 근태 엔티티 (출근시간, 퇴근시간, 상태)
domain/attendance/repository/AttendanceRepository.java
domain/attendance/service/AttendanceService.java # 출퇴근 기록, 근무시간 계산, 지각 체크
domain/attendance/controller/AttendanceController.java
domain/attendance/dto/AttendanceRequest.java
domain/attendance/dto/AttendanceResponse.java

# leave 도메인
domain/leave/entity/Leave.java               # 연차 엔티티 (총 연차, 사용 연차, 잔여)
domain/leave/repository/LeaveRepository.java
domain/leave/service/LeaveService.java       # 연차 조회, 자동 차감
domain/leave/controller/LeaveController.java
domain/leave/dto/LeaveRequest.java
domain/leave/dto/LeaveResponse.java
```

### 프론트 화면
- 출퇴근 버튼 화면
- 근무 기록 화면
- 연차 현황 화면

---

## 공통 영역 (global/) — 팀 전체 협의

| 파일 | 설명 | 우선 담당 |
|------|------|-----------|
| `global/config/SecurityConfig.java` | Security 설정 | 최혜인 |
| `global/security/JwtTokenProvider.java` | JWT 생성/검증 | 최혜인 |
| `global/security/JwtAuthenticationFilter.java` | JWT 필터 | 최혜인 |
| `global/exception/GlobalExceptionHandler.java` | 공통 예외 처리 | 팀 협의 |
| `global/response/ApiResponse.java` | 공통 응답 포맷 | 팀 협의 |
| `global/util/` | 유틸리티 | 필요 시 |

---

## 작업 순서 권장

1. **최혜인**: SecurityConfig + JWT 뼈대 먼저 (다른 팀원 API에 인증 필요)
2. **윤형진**: Employee 엔티티 먼저 (다른 도메인에서 참조)
3. **김민희**: Attendance는 Employee 의존 → Employee 완성 후 시작
4. **심소담**: AI는 독립적이라 병렬 작업 가능
