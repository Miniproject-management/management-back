package com.mini3.backend.global.security.custom;

import com.mini3.backend.domain.auth.enums.Role;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String empNo) throws UsernameNotFoundException {

        Employee employee = employeeRepository.findById(Long.parseLong(empNo))
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        Role role = getRole(employee);

        return new CustomUserDetails(employee, role);
    }

    private Role getRole(Employee employee) {

        if (employee.getDepartment().getDeptName().equals("HR")) {
            return Role.ROLE_ADMIN;
        }

        if (employee.getPosition().equals("팀장")) {
            return Role.ROLE_MANAGER;
        }

        return Role.ROLE_EMPLOYEE;
    }
}