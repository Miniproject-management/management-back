package com.mini3.backend.domain.leave.service;

import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import com.mini3.backend.domain.leave.dto.LeaveBalanceDto;
import com.mini3.backend.domain.leave.entity.LeaveBalance;
import com.mini3.backend.domain.leave.repository.LeaveBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceTest {

    @InjectMocks
    private LeaveBalanceService leaveBalanceService;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder().deptNo(1L).deptName("개발팀").build();
        employee = Employee.builder()
                .empNo(1L)
                .empName("루키즈")
                .department(dept)
                .build();
    }

    @Test
    @DisplayName("신규 연차 등록")
    void createOrUpdate_new() {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setEmpNo(1L);
        dto.setYear(2026);
        dto.setTotalLeave(new BigDecimal("15.00"));

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(leaveBalanceRepository.findDetailByEmployeeAndYear(1L, 2026)).willReturn(Optional.empty());
        given(leaveBalanceRepository.save(any(LeaveBalance.class))).willAnswer(i -> i.getArgument(0));

        LeaveBalance result = leaveBalanceService.createOrUpdate(dto);

        assertThat(result.getTotalLeave()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.getUsedLeave()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("기존 연차 수정")
    void createOrUpdate_existing() {
        LeaveBalance existing = LeaveBalance.builder()
                .balanceId(1L)
                .employee(employee)
                .year(2026)
                .totalLeave(new BigDecimal("15.00"))
                .usedLeave(new BigDecimal("3.00"))
                .build();

        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setEmpNo(1L);
        dto.setYear(2026);
        dto.setTotalLeave(new BigDecimal("20.00"));

        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(leaveBalanceRepository.findDetailByEmployeeAndYear(1L, 2026)).willReturn(Optional.of(existing));
        given(leaveBalanceRepository.save(any(LeaveBalance.class))).willAnswer(i -> i.getArgument(0));

        LeaveBalance result = leaveBalanceService.createOrUpdate(dto);

        assertThat(result.getTotalLeave()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getUsedLeave()).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("연차 조회 - 없으면 에러")
    void getBalance_notFound() {
        given(leaveBalanceRepository.findDetailByEmployeeAndYear(999L, 2026)).willReturn(Optional.empty());

        assertThatThrownBy(() -> leaveBalanceService.getBalance(999L, 2026))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("연차 정보가 없습니다");
    }
}
