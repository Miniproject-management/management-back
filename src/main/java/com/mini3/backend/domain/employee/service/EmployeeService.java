package com.mini3.backend.domain.employee.service;

import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.department.repository.DepartmentRepository;
import com.mini3.backend.domain.employee.dto.EmployeeRequest;
import com.mini3.backend.domain.employee.dto.EmployeeResponse;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.enums.Position;
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
     */
    @Transactional
    public Long createEmployee(EmployeeRequest request) {
        // 1. 부서 조회 (record 문법: deptNo())
        Department department = departmentRepository.findById(request.deptNo())
                .orElseThrow(() -> new EntityNotFoundException("해당 부서를 찾을 수 없습니다. ID: " + request.deptNo()));

        // 2. 사원 엔티티 빌드
        Employee employee = Employee.builder()
                .empName(request.empName())
                .department(department)
                .jobTitle(request.jobTitle())
                .position(parsePosition(request.position())) // Position Enum 객체 반환
                .hireDate(request.hireDate())
                .password(request.password()) // EmployeeRequest에 추가한 password 필드 사용
                .build();

        // 3. 저장 및 사번 반환
        return employeeRepository.save(employee).getEmpNo();
    }

    /**
     * [전체 사원 조회]
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
     */
    @Transactional
    public void updateEmployee(Long empNo, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("수정할 사원 정보를 찾을 수 없습니다."));

        Department department = departmentRepository.findById(request.deptNo())
                .orElseThrow(() -> new EntityNotFoundException("변경할 부서를 찾을 수 없습니다."));

        employee.setEmpName(request.empName());
        employee.setDepartment(department);
        employee.setJobTitle(request.jobTitle());
        employee.setPosition(parsePosition(request.position()));
        employee.setHireDate(request.hireDate());
        // 필요 시: employee.setPassword(request.password());
    }

    /**
     * [사원 삭제]
     */
    @Transactional
    public void deleteEmployee(Long empNo) {
        if (!employeeRepository.existsById(empNo)) {
            throw new EntityNotFoundException("삭제할 사원이 존재하지 않습니다.");
        }
        employeeRepository.deleteById(empNo);
    }

    /**
     * [Position 문자열을 Enum 객체로 변환]
     * 반환 타입이 Position(Enum)인 것이 핵심입니다.
     */
    private Position parsePosition(String positionName) {
        if (positionName == null || positionName.isBlank()) {
            throw new IllegalArgumentException("직급(position)은 필수 입력 값입니다.");
        }
        try {
            // 한글 상수명("사원", "팀장", "관리자")과 일치하는 Enum 객체 반환
            return Position.valueOf(positionName.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("허용되지 않는 직급입니다: " + positionName + " (사원, 팀장, 관리자 중 입력)");
        }
    }
}