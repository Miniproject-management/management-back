package com.mini3.backend.domain.employee.dto;

import com.mini3.backend.domain.employee.entity.Employee;
import lombok.*;
import java.time.LocalDate;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class EmployeeResponse {
    private Long empNo;
    private String empName;
    private String deptName;
    private String jobTitle;
    private String position;
    private LocalDate hireDate;

    // 엔티티를 응답 DTO로 변환하는 정적 팩토리 메서드
    public static EmployeeResponse from(Employee employee) {
        return EmployeeResponse.builder()
                .empNo(employee.getEmpNo())
                .empName(employee.getEmpName())
                .deptName(employee.getDepartment().getDeptName()) // 부서 엔티티에서 이름만 추출
                .jobTitle(employee.getJobTitle())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .build();
    }
}