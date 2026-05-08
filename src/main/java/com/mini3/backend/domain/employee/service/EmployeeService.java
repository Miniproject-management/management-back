package com.mini3.backend.domain.employee.service;

import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.department.repository.DepartmentRepository;
import com.mini3.backend.domain.employee.dto.EmployeeRequest;
import com.mini3.backend.domain.employee.dto.EmployeeResponse;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * [신규 사원 등록]
     * 명세서에는 없는 비밀번호를 엔티티의 필수 조건에 맞춰 "1234"로 초기화하여 저장합니다.
     */
    @Transactional
    public Long createEmployee(EmployeeRequest request) {
        // 1. 요청된 부서 번호로 부서 엔티티 조회 (연관관계 설정용)
        Department department = departmentRepository.findById(request.deptNo())
                .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다. ID: " + request.deptNo()));

        // 2. 엔티티 빌드 및 저장
        // 엔티티의 모든 nullable = false 컬럼을 확실히 매핑합니다.
        Employee employee = Employee.builder()
                .empName(request.empName())
                .department(department)
                .jobTitle(request.jobTitle())
                .position(request.position())
                .hireDate(request.hireDate())
                .password("1234") // 초기 비밀번호 자동 설정 (DB 제약 조건 충족)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);
        return savedEmployee.getEmpNo();
    }

    /**
     * [전체 사원 목록 조회]
     */
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * [사원 상세 조회]
     */
    public EmployeeResponse getEmployee(Long empNo) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("해당 사원을 찾을 수 없습니다. 사번: " + empNo));
        
        return EmployeeResponse.from(employee);
    }

    /**
     * [인사 정보 수정]
     * 명세서의 PUT /api/hr/employees/{empNo} 대응용
     */
    @Transactional
    public void updateEmployee(Long empNo, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("수정할 사원 정보가 없습니다."));

        Department department = departmentRepository.findById(request.deptNo())
                .orElseThrow(() -> new EntityNotFoundException("변경할 부서를 찾을 수 없습니다."));

        // 엔티티의 Setter나 별도 변경 메서드를 통해 정보 업데이트
        employee.setEmpName(request.empName());
        employee.setDepartment(department);
        employee.setJobTitle(request.jobTitle());
        employee.setPosition(request.position());
        // Dirty Checking에 의해 트랜잭션 종료 시 자동 반영됩니다.
    }
}