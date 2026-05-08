package com.mini3.backend.domain.employee.dto;

import lombok.*;
import java.time.LocalDate;
/*(응답용) */
@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class EmployeeResponse {
    private Long empNo;
    private String empName;
    private String deptName;
    private String jobTitle;
    private String position;
    private LocalDate hireDate;
}