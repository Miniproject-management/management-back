package com.mini3.backend.domain.employee.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping; // 沅뚰븳 泥댄겕瑜� �쐞�빐 異붽�
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mini3.backend.domain.employee.dto.EmployeeRequest;
import com.mini3.backend.domain.employee.dto.EmployeeResponse;
import com.mini3.backend.domain.employee.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 1. 신규 사원 등록
     * 권한: ADMIN(관리자) 또는 MANAGER(팀장)만 가능
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") 
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody EmployeeRequest request) {
        Long empNo = employeeService.createEmployee(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "empNo", empNo,
                "message", "등록 완료"
        ));
    }

    /**
     * 2. 전체 사원 목록 조회
     * 권한: 인증된 모든 사용자 (또는 필요시 ADMIN/MANAGER로 제한 가능)
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * 3. 사원 상세 조회
     */
    @GetMapping("/{empNo}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long empNo) {
        return ResponseEntity.ok(employeeService.getEmployee(empNo));
    }

    /**
     * 4. 사원 정보 수정
     * 권한: ADMIN 또는 MANAGER만 가능
     */
    @PutMapping("/{empNo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> updateEmployee(
            @PathVariable Long empNo, 
            @RequestBody EmployeeRequest request) {
        employeeService.updateEmployee(empNo, request);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    /**
     * 5. 사원 삭제
     * 권한: ADMIN만 가능 (삭제는 더 엄격하게 관리할 경우)
     */
    @DeleteMapping("/{empNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long empNo) {
        // 서비스에 deleteEmployee 구현 확인 필요
        // employeeService.deleteEmployee(empNo); 
        return ResponseEntity.noContent().build();
    }
}