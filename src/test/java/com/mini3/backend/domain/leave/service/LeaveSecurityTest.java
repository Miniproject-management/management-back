package com.mini3.backend.domain.leave.service;

import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.enums.Position;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import com.mini3.backend.domain.leave.dto.LeaveRequestDto;
import com.mini3.backend.domain.leave.entity.LeaveBalance;
import com.mini3.backend.domain.leave.entity.LeaveRequest;
import com.mini3.backend.domain.leave.enums.LeaveStatus;
import com.mini3.backend.domain.leave.repository.LeaveBalanceRepository;
import com.mini3.backend.domain.leave.repository.LeaveRequestRepository;
import com.mini3.backend.global.exception.InsufficientLeaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("🔒 보안 시나리오 테스트")
class LeaveSecurityTest {

    @InjectMocks
    private LeaveService leaveService;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private Employee attacker;
    private LeaveBalance balance;

    @BeforeEach
    void setUp() {

        Department dept = Department.builder()
                .deptNo(1L)
                .deptName("개발팀")
                .build();

        employee = Employee.builder()
                .empNo(1L)
                .empName("루키즈")
                .department(dept)
                .position(Position.사원)
                .build();

        attacker = Employee.builder()
                .empNo(999L)
                .empName("해커")
                .department(dept)
                .position(Position.사원)
                .build();

        balance = LeaveBalance.builder()
                .balanceId(1L)
                .employee(employee)
                .year(2026)
                .totalLeave(new BigDecimal("15.00"))
                .usedLeave(new BigDecimal("3.00"))
                .build();
    }

    @Nested
    @DisplayName("시나리오 1: XSS 공격")
    class XssAttack {

        @Test
        @DisplayName("reason에 <script> 태그 삽입 시 이스케이프 처리")
        void xss_scriptTag() {

            LeaveRequestDto dto = new LeaveRequestDto();

            dto.setEmpNo(1L);
            dto.setLeaveType("연차");
            dto.setStartDate(LocalDate.of(2026, 7, 1));
            dto.setEndDate(LocalDate.of(2026, 7, 1));
            dto.setRequestDays(new BigDecimal("1"));
            dto.setReason("<script>alert('해킹')</script>");

            given(employeeRepository.findById(1L))
                    .willReturn(Optional.of(employee));

            given(leaveBalanceRepository.findByEmployee_EmpNoAndYear(1L, 2026))
                    .willReturn(Optional.of(balance));

            given(leaveRequestRepository.findOverlapping(eq(1L), any(), any()))
                    .willReturn(Collections.emptyList());

            given(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .willAnswer(i -> i.getArgument(0));

            LeaveRequest result = leaveService.applyLeave(dto);

            assertThat(result.getReason()).doesNotContain("<script>");
            assertThat(result.getReason()).contains("&lt;script&gt;");
        }

        @Test
        @DisplayName("reason에 HTML 태그 삽입 시 이스케이프 처리")
        void xss_htmlTag() {

            LeaveRequestDto dto = new LeaveRequestDto();

            dto.setEmpNo(1L);
            dto.setLeaveType("연차");
            dto.setStartDate(LocalDate.of(2026, 7, 2));
            dto.setEndDate(LocalDate.of(2026, 7, 2));
            dto.setRequestDays(new BigDecimal("1"));
            dto.setReason("<img src=x onerror=alert('hack')>");

            given(employeeRepository.findById(1L))
                    .willReturn(Optional.of(employee));

            given(leaveBalanceRepository.findByEmployee_EmpNoAndYear(1L, 2026))
                    .willReturn(Optional.of(balance));

            given(leaveRequestRepository.findOverlapping(eq(1L), any(), any()))
                    .willReturn(Collections.emptyList());

            given(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .willAnswer(i -> i.getArgument(0));

            LeaveRequest result = leaveService.applyLeave(dto);

            assertThat(result.getReason()).doesNotContain("<img");
        }
    }

    @Nested
    @DisplayName("시나리오 2: 다른 사람 휴가 조작 시도")
    class UnauthorizedAccess {

        @Test
        @DisplayName("다른 사원의 휴가를 취소하려고 시도 → 차단")
        void cancel_otherPersonLeave() {

            LeaveRequest request = LeaveRequest.builder()
                    .leaveId(1L)
                    .employee(employee)
                    .leaveStatus(LeaveStatus.PENDING_MANAGER)
                    .build();

            given(leaveRequestRepository.findById(1L))
                    .willReturn(Optional.of(request));

            assertThatThrownBy(() -> leaveService.cancel(1L, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("본인의 신청만 취소할 수 있습니다");
        }

        @Test
        @DisplayName("이미 승인된 휴가를 취소하려고 시도 → 차단")
        void cancel_approvedLeave() {

            LeaveRequest request = LeaveRequest.builder()
                    .leaveId(1L)
                    .employee(employee)
                    .leaveStatus(LeaveStatus.APPROVED)
                    .build();

            given(leaveRequestRepository.findById(1L))
                    .willReturn(Optional.of(request));

            assertThatThrownBy(() -> leaveService.cancel(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 승인된 휴가는 취소할 수 없습니다");
        }

        @Test
        @DisplayName("이미 승인 완료된 휴가를 다시 승인하려고 시도 → 차단")
        void approve_alreadyApproved() {

            LeaveRequest request = LeaveRequest.builder()
                    .leaveId(1L)
                    .employee(employee)
                    .leaveStatus(LeaveStatus.APPROVED)
                    .build();

            given(leaveRequestRepository.findById(1L))
                    .willReturn(Optional.of(request));

            given(employeeRepository.findById(2L))
                    .willReturn(Optional.of(attacker));

            assertThatThrownBy(() -> leaveService.approve(1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("승인할 수 없는 상태입니다");
        }
    }

    @Nested
    @DisplayName("시나리오 3: 비정상 데이터 입력")
    class InvalidInput {

        @Test
        @DisplayName("종료일이 시작일보다 앞선 경우 → 차단")
        void endDate_beforeStartDate() {

            LeaveRequestDto dto = new LeaveRequestDto();

            dto.setEmpNo(1L);
            dto.setLeaveType("연차");
            dto.setStartDate(LocalDate.of(2026, 7, 10));
            dto.setEndDate(LocalDate.of(2026, 7, 5));
            dto.setRequestDays(new BigDecimal("1"));
            dto.setReason("테스트");

            given(employeeRepository.findById(1L))
                    .willReturn(Optional.of(employee));

            assertThatThrownBy(() -> leaveService.applyLeave(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("종료일이 시작일보다 앞설 수 없습니다");
        }

        @Test
        @DisplayName("30일 초과 연차 신청 → 차단")
        void over30Days() {

            LeaveRequestDto dto = new LeaveRequestDto();

            dto.setEmpNo(1L);
            dto.setLeaveType("연차");
            dto.setStartDate(LocalDate.of(2026, 7, 1));
            dto.setEndDate(LocalDate.of(2026, 8, 15));
            dto.setRequestDays(new BigDecimal("30"));
            dto.setReason("장기 휴가");

            given(employeeRepository.findById(1L))
                    .willReturn(Optional.of(employee));

            assertThatThrownBy(() -> leaveService.applyLeave(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1회 최대 30일까지만 신청할 수 있습니다");
        }

        @Test
        @DisplayName("연차 부족한데 신청 → 차단")
        void insufficientBalance() {

            balance.setUsedLeave(new BigDecimal("14.50"));

            LeaveRequestDto dto = new LeaveRequestDto();

            dto.setEmpNo(1L);
            dto.setLeaveType("연차");
            dto.setStartDate(LocalDate.of(2026, 7, 1));
            dto.setEndDate(LocalDate.of(2026, 7, 2));
            dto.setRequestDays(new BigDecimal("2"));
            dto.setReason("여행");

            given(employeeRepository.findById(1L))
                    .willReturn(Optional.of(employee));

            given(leaveBalanceRepository.findByEmployee_EmpNoAndYear(1L, 2026))
                    .willReturn(Optional.of(balance));

            assertThatThrownBy(() -> leaveService.applyLeave(dto))
                    .isInstanceOf(InsufficientLeaveException.class)
                    .hasMessageContaining("남은 연차가 부족합니다");
        }

        @Test
        @DisplayName("존재하지 않는 사원으로 신청 → 차단")
        void nonExistentEmployee() {

            LeaveRequestDto dto = new LeaveRequestDto();

            dto.setEmpNo(9999L);
            dto.setLeaveType("연차");
            dto.setStartDate(LocalDate.of(2026, 7, 1));
            dto.setEndDate(LocalDate.of(2026, 7, 1));
            dto.setRequestDays(new BigDecimal("1"));

            given(employeeRepository.findById(9999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.applyLeave(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사원을 찾을 수 없습니다");
        }
    }
}