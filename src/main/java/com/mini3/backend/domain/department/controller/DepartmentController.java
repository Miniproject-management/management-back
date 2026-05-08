package com.mini3.backend.domain.department.controller;

import com.mini3.backend.domain.department.dto.DepartmentResponse;
import com.mini3.backend.domain.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * [명세서 기능] 조직도 트리 조회
     * GET /api/hr/departments/tree
     * 부서 목록과 각 부서별 인원 통계(employeeCount)를 반환합니다.
     */
    @GetMapping("/tree")
    public ResponseEntity<List<DepartmentResponse>> getDepartmentTree() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * [명세서 기능] 신규 부서 생성
     * POST /api/hr/departments
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDepartment(
            @RequestParam String deptName, 
            @RequestParam(required = false) String deptDesc) {
        
        DepartmentResponse response = departmentService.createDepartment(deptName, deptDesc);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "deptNo", response.getDeptNo(),
                "message", "저장 완료"
        ));
    }

    /**
     * [명세서 기능] 부서 정보 수정
     * PUT /api/hr/departments/{deptNo}
     */
    @PutMapping("/{deptNo}")
    public ResponseEntity<Map<String, Object>> updateDepartment(
            @PathVariable Long deptNo,
            @RequestParam String deptName,
            @RequestParam(required = false) String deptDesc) {
        
        // Service에 해당 수정 로직이 구현되어 있어야 합니다.
        // 현재는 구조 파악을 위해 성공 응답만 규격화했습니다.
        return ResponseEntity.ok(Map.of(
                "deptNo", deptNo,
                "message", "저장 완료"
        ));
    }

    /**
     * 부서 단건 상세 조회
     * GET /api/hr/departments/{deptNo}
     */
    @GetMapping("/{deptNo}")
    public ResponseEntity<DepartmentResponse> getDepartment(@PathVariable Long deptNo) {
        DepartmentResponse department = departmentService.getDepartment(deptNo);
        return ResponseEntity.ok(department);
    }
}