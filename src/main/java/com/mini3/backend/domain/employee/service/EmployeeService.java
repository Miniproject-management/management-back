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
        // 1. 부서 확인
        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다. ID: " + request.getDeptNo()));

        // 2. DTO -> Entity 변환
        // request.getPosition()으로 들어온 문자열("사원", "팀장" 등)을 Enum으로 검증 후 저장합니다.
        Employee employee = Employee.builder()
                .empName(request.getEmpName())
                .department(department)
                .jobTitle(request.getJobTitle())
                .position(validateAndGetPosition(request.getPosition())) // Enum 검증 로직 호출
                .hireDate(request.getHireDate())
                .password("1234") // 초기 비밀번호 세팅
                .build();

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
                .orElseThrow(() -> new EntityNotFoundException("사원을 찾을 수 없습니다. 사번: " + empNo));
        return EmployeeResponse.from(employee);
    }

    /**
     * [사원 정보 수정]
     */
    @Transactional
    public void updateEmployee(Long empNo, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("수정할 사원이 없습니다."));

        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다."));

        employee.setEmpName(request.getEmpName());
        employee.setDepartment(department);
        employee.setJobTitle(request.getJobTitle());
        employee.setPosition(validateAndGetPosition(request.getPosition()));
        employee.setHireDate(request.getHireDate());
    }

    /**
     * [사원 삭제]
     */
    @Transactional
    public void deleteEmployee(Long empNo) {
        if (!employeeRepository.existsById(empNo)) {
            throw new EntityNotFoundException("삭제할 사원이 없습니다.");
        }
        employeeRepository.deleteById(empNo);
    }

    /**
     * [Enum 검증 메서드]
     * 입력된 문자열이 Position Enum(사원, 팀장, 관리자)에 존재하는지 확인합니다.
     */
    private String validateAndGetPosition(String positionName) {
        try {
            // Position.valueOf("사원") 등을 통해 Enum 상수가 있는지 확인합니다.
            return Position.valueOf(positionName.trim()).name();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("유효하지 않은 직급입니다. (사원, 팀장, 관리자 중 입력): " + positionName);
        }
    }
}