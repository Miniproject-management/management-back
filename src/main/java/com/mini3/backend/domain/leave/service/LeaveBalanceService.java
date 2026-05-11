package com.mini3.backend.domain.leave.service;

import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import com.mini3.backend.domain.leave.dto.LeaveBalanceDto;
import com.mini3.backend.domain.leave.entity.LeaveBalance;
import com.mini3.backend.domain.leave.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveBalance createOrUpdate(LeaveBalanceDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmpNo())
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));

        LeaveBalance balance = leaveBalanceRepository
                .findDetailByEmployeeAndYear(dto.getEmpNo(), dto.getYear())
                .orElse(LeaveBalance.builder()
                        .employee(employee)
                        .year(dto.getYear())
                        .usedLeave(BigDecimal.ZERO)
                        .build());

        balance.setTotalLeave(dto.getTotalLeave());
        return leaveBalanceRepository.save(balance);
    }

    public LeaveBalance getBalance(Long empNo, Integer year) {
        return leaveBalanceRepository.findDetailByEmployeeAndYear(empNo, year)
                .orElseThrow(() -> new IllegalArgumentException("연차 정보가 없습니다."));
    }
}
