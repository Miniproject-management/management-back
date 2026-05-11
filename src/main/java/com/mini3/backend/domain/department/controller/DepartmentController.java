package com.mini3.backend.domain.department.controller;

import com.mini3.backend.domain.department.dto.DepartmentRequest;
import com.mini3.backend.domain.department.dto.DepartmentResponse;
import com.mini3.backend.domain.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 부서 관리 컨트롤러
 * 부서의 등록, 수정, 조회(조직도 트리 포함) 기능을 담당합니다.
 */
@RestController
@RequestMapping("/api/hr/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * [기능] 전체 부서 조직도 조회
     * GET /api/hr/departments/tree
     * @return 부서 정보와 각 부서별 소속 인원수(employeeCount) 리스트
     */
    @GetMapping("/tree")
    public ResponseEntity<List<DepartmentResponse>> getDepartmentTree() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * [기능] 신규 부서 등록
     * POST /api/hr/departments
     * @param request JSON 바디 (deptName, deptDesc)
     * @return 생성된 부서 번호(deptNo)와 성공 메시지
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDepartment(@RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "deptNo", response.getDeptNo(),
                "message", "저장 완료"
        ));
    }

    /**
     * [기능] 부서 정보 수정
     * PUT /api/hr/departments/{deptNo}
     * @param deptNo 수정할 부서의 고유 번호
     * @param request JSON 바디 (수정할 deptName, deptDesc)
     * @return 수정된 부서 번호와 성공 메시지
     */
    @PutMapping("/{deptNo}")
    public ResponseEntity<Map<String, Object>> updateDepartment(
            @PathVariable Long deptNo,
            @RequestBody DepartmentRequest request) {
        
        departmentService.updateDepartment(deptNo, request);
        return ResponseEntity.ok(Map.of(
                "deptNo", deptNo,
                "message", "저장 완료"
        ));
    }

    /**
     * [기능] 부서 단건 상세 조회
     * GET /api/hr/departments/{deptNo}
     * 조회할 부서의 고유 번호
     * 특정 부서의 상세 정보와 해당 부서 인원수
     */
    @GetMapping("/{deptNo}")
    public ResponseEntity<DepartmentResponse> getDepartment(@PathVariable Long deptNo) {
        DepartmentResponse department = departmentService.getDepartment(deptNo);
        return ResponseEntity.ok(department);
    }
}