package com.mini3.backend.domain.leave.service;

import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import com.mini3.backend.domain.leave.dto.LeaveRequestDto;
import com.mini3.backend.domain.leave.entity.LeaveBalance;
import com.mini3.backend.domain.leave.entity.LeaveRequest;
import com.mini3.backend.domain.leave.enums.LeaveStatus;
import com.mini3.backend.domain.leave.repository.LeaveBalanceRepository;
import com.mini3.backend.domain.leave.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @InjectMocks
    private LeaveService leaveService;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private Employee manager;
    private Employee hrManager;
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
                .position("사원")
                .build();

        manager = Employee.builder()
                .empNo(2L)
                .empName("김팀장")
                .department(dept)
                .position("팀장")
                .build();

        hrManager = Employee.builder()
                .empNo(3L)
                .empName("박인사")
                .position("인사담당자")
                .build();

        balance = LeaveBalance.builder()
                .balanceId(1L)
                .employee(employee)
                .year(2026)
                .totalLeave(new BigDecimal("15.00"))
                .usedLeave(new BigDecimal("3.00"))
                .build();
    }

    @Test
    @DisplayName("연차 신청 성공")
    void applyLeave_success() {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmpNo(1L);
        dto.setLeaveType("연차");
        dto.setStartDate(LocalDate.of(2026, 6, 2));
        dto.setEndDate(LocalDate.of(2026, 6, 3));
        dto.setReason("개인 일정");

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(leaveBalanceRepository.findByEmployee_EmpNoAndYear(1L, 2026)).willReturn(Optional.of(balance));
        given(leaveRequestRepository.findOverlapping(eq(1L), any(), any())).willReturn(Collections.emptyList());
        given(leaveRequestRepository.save(any(LeaveRequest.class))).willAnswer(i -> i.getArgument(0));

        LeaveRequest result = leaveService.applyLeave(dto);

        assertThat(result.getLeaveStatus()).isEqualTo(LeaveStatus.PENDING_MANAGER);
        assertThat(result.getLeaveDays()).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    @DisplayName("남은 연차 부족 시 에러")
    void applyLeave_insufficientBalance() {
        balance.setUsedLeave(new BigDecimal("14.00"));

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmpNo(1L);
        dto.setLeaveType("연차");
        dto.setStartDate(LocalDate.of(2026, 6, 1));
        dto.setEndDate(LocalDate.of(2026, 6, 5));
        dto.setReason("여행");

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(leaveBalanceRepository.findByEmployee_EmpNoAndYear(1L, 2026)).willReturn(Optional.of(balance));

        assertThatThrownBy(() -> leaveService.applyLeave(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("남은 연차가 부족합니다");
    }

    @Test
    @DisplayName("중복 날짜 신청 시 에러")
    void applyLeave_overlapping() {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmpNo(1L);
        dto.setLeaveType("연차");
        dto.setStartDate(LocalDate.of(2026, 6, 2));
        dto.setEndDate(LocalDate.of(2026, 6, 2));
        dto.setReason("개인 일정");

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(leaveBalanceRepository.findByEmployee_EmpNoAndYear(1L, 2026)).willReturn(Optional.of(balance));
        given(leaveRequestRepository.findOverlapping(eq(1L), any(), any()))
                .willReturn(List.of(LeaveRequest.builder().build()));

        assertThatThrownBy(() -> leaveService.applyLeave(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 해당 기간에 신청된 휴가가 있습니다");
    }

    @Test
    @DisplayName("팀장 승인: PENDING_MANAGER → PENDING_HR")
    void approve_byManager() {
        LeaveRequest request = LeaveRequest.builder()
                .leaveId(1L)
                .employee(employee)
                .leaveStatus(LeaveStatus.PENDING_MANAGER)
                .leaveDays(new BigDecimal("1"))
                .startDate(LocalDate.of(2026, 6, 2))
                .build();

        given(leaveRequestRepository.findById(1L)).willReturn(Optional.of(request));
        given(employeeRepository.findById(2L)).willReturn(Optional.of(manager));
        given(leaveRequestRepository.save(any())).willAnswer(i -> i.getArgument(0));

        LeaveRequest result = leaveService.approve(1L, 2L);

        assertThat(result.getLeaveStatus()).isEqualTo(LeaveStatus.PENDING_HR);
        assertThat(result.getApprovedBy().getEmpNo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("인사팀 최종 승인: PENDING_HR → APPROVED + 연차 차감")
    void approve_byHR() {
        LeaveRequest request = LeaveRequest.builder()
                .leaveId(1L)
                .employee(employee)
                .leaveStatus(LeaveStatus.PENDING_HR)
                .leaveDays(new BigDecimal("1"))
                .startDate(LocalDate.of(2026, 6, 2))
                .build();

        given(leaveRequestRepository.findById(1L)).willReturn(Optional.of(request));
        given(employeeRepository.findById(3L)).willReturn(Optional.of(hrManager));
        given(leaveBalanceRepository.findByEmployee_EmpNoAndYear(1L, 2026)).willReturn(Optional.of(balance));
        given(leaveRequestRepository.save(any())).willAnswer(i -> i.getArgument(0));

        LeaveRequest result = leaveService.approve(1L, 3L);

        assertThat(result.getLeaveStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(balance.getUsedLeave()).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    @Test
    @DisplayName("반려 처리")
    void reject_success() {
        LeaveRequest request = LeaveRequest.builder()
                .leaveId(1L)
                .employee(employee)
                .leaveStatus(LeaveStatus.PENDING_MANAGER)
                .build();

        given(leaveRequestRepository.findById(1L)).willReturn(Optional.of(request));
        given(employeeRepository.findById(2L)).willReturn(Optional.of(manager));
        given(leaveRequestRepository.save(any())).willAnswer(i -> i.getArgument(0));

        LeaveRequest result = leaveService.reject(1L, 2L);

        assertThat(result.getLeaveStatus()).isEqualTo(LeaveStatus.REJECTED);
    }

    @Test
    @DisplayName("신청 취소 성공")
    void cancel_success() {
        LeaveRequest request = LeaveRequest.builder()
                .leaveId(1L)
                .employee(employee)
                .leaveStatus(LeaveStatus.PENDING_MANAGER)
                .isActive("Y")
                .build();

        given(leaveRequestRepository.findById(1L)).willReturn(Optional.of(request));
        given(leaveRequestRepository.save(any())).willAnswer(i -> i.getArgument(0));

        LeaveRequest result = leaveService.cancel(1L, 1L);

        assertThat(result.getLeaveStatus()).isEqualTo(LeaveStatus.CANCELED);
        assertThat(result.getIsActive()).isEqualTo("N");
    }

    @Test
    @DisplayName("이미 승인된 휴가 취소 불가")
    void cancel_alreadyApproved() {
        LeaveRequest request = LeaveRequest.builder()
                .leaveId(1L)
                .employee(employee)
                .leaveStatus(LeaveStatus.APPROVED)
                .build();

        given(leaveRequestRepository.findById(1L)).willReturn(Optional.of(request));

        assertThatThrownBy(() -> leaveService.cancel(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 승인된 휴가는 취소할 수 없습니다");
    }

    @Test
    @DisplayName("다른 사원의 신청 취소 불가")
    void cancel_notOwner() {
        LeaveRequest request = LeaveRequest.builder()
                .leaveId(1L)
                .employee(employee)
                .leaveStatus(LeaveStatus.PENDING_MANAGER)
                .build();

        given(leaveRequestRepository.findById(1L)).willReturn(Optional.of(request));

        assertThatThrownBy(() -> leaveService.cancel(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 신청만 취소할 수 있습니다");
    }

    @Test
    @DisplayName("주말 제외 영업일 계산: 금~월 = 2일")
    void calculateBusinessDays_excludeWeekend() {
        LocalDate friday = LocalDate.of(2026, 6, 5);
        LocalDate monday = LocalDate.of(2026, 6, 8);

        BigDecimal result = leaveService.calculateBusinessDays(friday, monday, "연차");

        assertThat(result).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    @DisplayName("반차는 0.5일")
    void calculateBusinessDays_halfDay() {
        LocalDate date = LocalDate.of(2026, 6, 2);

        BigDecimal result = leaveService.calculateBusinessDays(date, date, "오전반차");

        assertThat(result).isEqualByComparingTo(new BigDecimal("0.5"));
    }
}
