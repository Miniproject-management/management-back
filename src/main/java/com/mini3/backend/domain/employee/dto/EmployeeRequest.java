package com.mini3.backend.domain.employee.dto;

import java.time.LocalDate;

public record EmployeeRequest(
    String empName,
    Long deptNo,
    String jobTitle,
    String position,
    LocalDate hireDate,
    String password
) { }