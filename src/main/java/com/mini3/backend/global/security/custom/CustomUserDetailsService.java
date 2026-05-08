package com.mini3.backend.global.security.custom;

import com.mini3.backend.domain.auth.enums.Role;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.enums.Position;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String empNo)
            throws UsernameNotFoundException {

        Employee employee = employeeRepository
                .findByEmpNoWithDepartment(Long.parseLong(empNo))
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        Role role = getRole(employee);

        return new CustomUserDetails(employee, role);
    }

    private Role getRole(Employee employee) {

        // 인사팀이면 무조건 관리자
        if (employee.getDepartment().getDeptName().equals("인사팀")) {
            return Role.ROLE_ADMIN;
        }

        // 일반 부서 팀장
        if (employee.getPosition() == Position.팀장) {
            return Role.ROLE_MANAGER;
        }

        // 일반 사원
        return Role.ROLE_EMPLOYEE;
    }
}