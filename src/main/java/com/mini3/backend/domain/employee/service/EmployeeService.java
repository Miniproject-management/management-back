package com.mini3.backend.domain.employee.service;

import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.department.repository.DepartmentRepository;
import com.mini3.backend.domain.employee.dto.EmployeeRequest;
import com.mini3.backend.domain.employee.dto.EmployeeResponse;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.enums.Position; // Enum 임포트
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
     * Main 브랜치의 방식대로 요청(Request)에서 비밀번호를 가져와 저장합니다.
     */
    @Transactional
    public Long createEmployee(EmployeeRequest request) {
        // 1. 부서 존재 여부 확인
        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new EntityNotFoundException("해당 부서를 찾을 수 없습니다. ID: " + request.getDeptNo()));

        // 2. DTO -> Entity 변환 (Main 방식 준수)
        Employee employee = Employee.builder()
                .empName(request.getEmpName())
                .department(department)
                .jobTitle(request.getJobTitle())
                .position(parsePosition(request.getPosition())) // Enum 변환 로직 사용
                .hireDate(request.getHireDate())
                .password(request.getPassword()) // Main 브랜치 방식: 요청받은 비번 그대로 사용
                .build();

        // 3. 저장 및 생성된 사번 반환
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
     * [사원 정보 수정]
     */
    @Transactional
    public void updateEmployee(Long empNo, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("수정할 사원 정보를 찾을 수 없습니다."));

        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new EntityNotFoundException("변경할 부서를 찾을 수 없습니다."));

        employee.setEmpName(request.getEmpName());
        employee.setDepartment(department);
        employee.setJobTitle(request.getJobTitle());
        employee.setPosition(parsePosition(request.getPosition()));
        employee.setHireDate(request.getHireDate());
        // 수정 시에도 비번을 업데이트한다면 아래 추가 (선택사항)
        // employee.setPassword(request.getPassword()); 
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
     * [Position Enum 변환 및 검증]
     */
    private String parsePosition(String position) {
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("직급(position)은 필수 입력 값입니다.");
        }
        try {
            // 한글 Enum 상수("사원", "팀장", "관리자")와 매칭되는지 확인
            return Position.valueOf(position.trim()).name();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("허용되지 않는 직급입니다: " + position + " (사원, 팀장, 관리자 중 입력)");
        }
    }
}