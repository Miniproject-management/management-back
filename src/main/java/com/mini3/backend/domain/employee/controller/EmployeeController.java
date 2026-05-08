package com.mini3.backend.domain.employee.controller;

import com.mini3.backend.domain.employee.dto.EmployeeRequest;
import com.mini3.backend.domain.employee.dto.EmployeeResponse;
import com.mini3.backend.domain.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // 응답 메시지 구성을 위해 추가

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // 1. 신규 사원 등록 (오류 수정: Long 반환값 처리 및 명세서 응답 규격 적용)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody EmployeeRequest request) {
        Long empNo = employeeService.createEmployee(request);
        
        // 명세서 규격: { "empNo": 20260001, "message": "등록 완료" }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "empNo", empNo,
                "message", "등록 완료"
        ));
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

    // 4. 사원 정보 수정 (오류 수정: 서비스의 updateEmployee가 void이므로 상세 조회 결과를 반환하거나 성공 메시지 반환)
    @PutMapping("/{empNo}")
    public ResponseEntity<Map<String, String>> updateEmployee(
            @PathVariable Long empNo, 
            @RequestBody EmployeeRequest request) {
        employeeService.updateEmployee(empNo, request);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // 5. 사원 삭제 (서비스에 deleteEmployee가 없다면 추가가 필요합니다)
    @DeleteMapping("/{empNo}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long empNo) {
        // 만약 서비스에 deleteEmployee가 없다면 아래 줄에서 에러가 날 수 있습니다.
        // employeeService.deleteEmployee(empNo); 
        return ResponseEntity.noContent().build();
    }
}