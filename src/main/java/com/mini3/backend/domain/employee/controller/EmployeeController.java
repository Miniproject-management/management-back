package com.mini3.backend.domain.employee.controller;

import com.mini3.backend.domain.employee.dto.EmployeeRequest;
import com.mini3.backend.domain.employee.dto.EmployeeResponse;
import com.mini3.backend.domain.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // 1. 신규 사원 등록
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 전체 사원 목록 조회
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // 3. 사원 상세 조회
    @GetMapping("/{empNo}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long empNo) {
        return ResponseEntity.ok(employeeService.getEmployee(empNo));
    }

    // 4. 사원 정보 수정
    @PutMapping("/{empNo}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long empNo, 
            @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(empNo, request));
    }

    // 5. 사원 삭제
    @DeleteMapping("/{empNo}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long empNo) {
        employeeService.deleteEmployee(empNo);
        return ResponseEntity.noContent().build();
    }
}