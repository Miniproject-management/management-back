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
import org.springframework.security.crypto.password.PasswordEncoder; // 암호화 주입
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
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 등록한 BCrypt 기계

    /**
     * [신규 사원 등록]
     * 암호화 로직을 추가하여 사원을 생성하고 사번을 반환합니다.
     */
    @Transactional
    public Long createEmployee(EmployeeRequest request) {
        // 1. 부서 조회
        Department department = departmentRepository.findById(request.deptNo())
                .orElseThrow(() -> new EntityNotFoundException("해당 부서를 찾을 수 없습니다. ID: " + request.deptNo()));

        // 2. 비밀번호 암호화 (BCrypt 적용)
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. 사원 엔티티 빌드
        Employee employee = Employee.builder()
                .empName(request.empName())
                .department(department)
                .jobTitle(request.jobTitle())
                .position(parsePosition(request.position())) // 기존 파싱 로직 유지
                .hireDate(request.hireDate())
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .build();

        // 4. 저장 및 사번 반환
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
     * 수정 시에도 비밀번호가 입력되었다면 암호화를 거쳐 저장합니다.
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

        // 비밀번호 수정 요청이 있을 경우 암호화 후 반영
        if (request.password() != null && !request.password().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.password()));
        }
    }

    /**
     * [사원 삭제]
     * 기존에 누락되었던 삭제 기능을 복구했습니다.
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
     * 기존의 엄격한 유효성 검사 로직을 그대로 유지합니다.
     */
    private Position parsePosition(String positionName) {
        if (positionName == null || positionName.isBlank()) {
            throw new IllegalArgumentException("직급(position)은 필수 입력 값입니다.");
        }
        try {
            return Position.valueOf(positionName.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("허용되지 않는 직급입니다: " + positionName + " (사원, 팀장, 관리자 중 입력)");
        }
    }
}