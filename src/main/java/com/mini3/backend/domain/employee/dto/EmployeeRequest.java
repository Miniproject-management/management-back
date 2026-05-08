package com.mini3.backend.domain.employee.dto;

import java.time.LocalDate;

public record EmployeeRequest(
    String empName,
    Long deptNo,
    String jobTitle,  // 엔티티의 jobTitle과 매칭
    String position,  // 엔티티의 position과 매칭
    LocalDate hireDate
) {}