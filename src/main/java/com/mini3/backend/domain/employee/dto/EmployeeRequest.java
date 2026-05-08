package com.mini3.backend.domain.employee.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class EmployeeRequest {
    private String empName;
    private Long deptNo;
    private String jobTitle;
    private String position;
    private LocalDate hireDate;
    private String password;
}