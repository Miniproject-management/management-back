package com.mini3.backend.domain.department.service;

import com.mini3.backend.domain.department.dto.DepartmentResponse;
import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.department.repository.DepartmentRepository;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 모드 (성능 최적화)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * 모든 부서 목록 조회 (인원수 포함)
     * 조직도 리스트나 부서 선택 드롭다운 등에서 사용됩니다.
     */
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(dept -> {
                    // EmployeeRepository를 사용하여 해당 부서 번호를 가진 사원의 수를 카운트
                    // SQL: SELECT COUNT(*) FROM employees WHERE dept_no = ?
                    long count = employeeRepository.findByDepartmentDeptNo(dept.getDeptNo()).size();
                    
                    // 엔티티와 카운트 결과를 DTO로 변환
                    return DepartmentResponse.from(dept, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 부서 상세 조회 (인원수 포함)
     */
    public DepartmentResponse getDepartment(Long deptNo) {
        Department department = departmentRepository.findById(deptNo)
                .orElseThrow(() -> new EntityNotFoundException("해당 부서를 찾을 수 없습니다. ID: " + deptNo));

        long count = employeeRepository.findByDepartmentDeptNo(deptNo).size();
        
        return DepartmentResponse.from(department, count);
    }

    /**
     * 부서 신규 등록 (필요 시 사용)
     */
    @Transactional
    public DepartmentResponse createDepartment(String deptName, String deptDesc) {
        // 중복 체크 (기존에 만드신 existsByDeptName 활용)
        if (departmentRepository.existsByDeptName(deptName)) {
            throw new IllegalArgumentException("이미 존재하는 부서명입니다.");
        }

        Department department = Department.builder()
                .deptName(deptName)
                .deptDesc(deptDesc)
                .build();

        Department saved = departmentRepository.save(department);
        return DepartmentResponse.from(saved, 0L); // 신설 부서는 인원이 0명
    }
}