package com.mini3.backend.domain.department.dto;

import com.mini3.backend.domain.department.entity.Department;
import lombok.*;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DepartmentResponse {
    private Long deptNo;
    private String deptName;
    private String deptDesc;
    private long employeeCount; // 사원 수 필드 추가
    private String leaderName; // 팀장 이름 필드 추가

    // 기존 from 메서드에 count 파라미터 추가
    public static DepartmentResponse from(Department department, long count, String leaderName) {
        return DepartmentResponse.builder()
                .deptNo(department.getDeptNo())
                .deptName(department.getDeptName())
                .deptDesc(department.getDeptDesc())
                .employeeCount(count)
                .leaderName(leaderName)
                .build();
    }
}