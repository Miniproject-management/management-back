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
import com.mini3.backend.domain.employee.enums.Position;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 신규 사원 등록
     */
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // 1. 부서 존재 여부 확인
        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new EntityNotFoundException("해당 부서를 찾을 수 없습니다."));

        // 2. DTO -> Entity 변환
        Employee employee = Employee.builder()
                .empName(request.getEmpName())
                .department(department)
                .jobTitle(request.getJobTitle())
                .position(Position.valueOf(request.getPosition()))
                .hireDate(request.getHireDate())
                .password(request.getPassword()) // 추후 최혜인님 담당 PasswordEncoder 연동 필요
                .build();

        // 3. 저장 및 응답 DTO 반환
        Employee savedEmployee = employeeRepository.save(employee);
        return EmployeeResponse.from(savedEmployee);
    }

    /**
     * 사원 상세 조회
     */
    public EmployeeResponse getEmployee(Long empNo) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("해당 사원을 찾을 수 없습니다."));
        return EmployeeResponse.from(employee);
    }

    /**
     * 전체 사원 목록 조회
     */
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사원 정보 수정 (부서 이동, 직급 변경 등)
     */
    @Transactional
    public EmployeeResponse updateEmployee(Long empNo, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("해당 사원을 찾을 수 없습니다."));

        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new EntityNotFoundException("해당 부서를 찾을 수 없습니다."));

        // 엔티티 업데이트 (Setter 또는 비즈니스 메서드 활용)
        employee.setEmpName(request.getEmpName());
        employee.setDepartment(department);
        employee.setJobTitle(request.getJobTitle());
        employee.setPosition(Position.valueOf(request.getPosition()));
        
        return EmployeeResponse.from(employee);
    }

    /**
     * 사원 삭제 (퇴사 처리 등)
     */
    @Transactional
    public void deleteEmployee(Long empNo) {
        if (!employeeRepository.existsById(empNo)) {
            throw new EntityNotFoundException("해당 사원이 존재하지 않습니다.");
        }
        employeeRepository.deleteById(empNo);
    }
}