package com.mini3.backend.domain.department.service;

import com.mini3.backend.domain.department.dto.DepartmentRequest;
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
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(dept -> {
                    long count = employeeRepository.findByDepartmentDeptNo(dept.getDeptNo()).size();
                    return DepartmentResponse.from(dept, count);
                })
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartment(Long deptNo) {
        Department department = departmentRepository.findById(deptNo)
                .orElseThrow(() -> new EntityNotFoundException("해당 부서를 찾을 수 없습니다. ID: " + deptNo));
        long count = employeeRepository.findByDepartmentDeptNo(deptNo).size();
        return DepartmentResponse.from(department, count);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByDeptName(request.deptName())) {
            throw new IllegalArgumentException("이미 존재하는 부서명입니다.");
        }

        Department department = Department.builder()
                .deptName(request.deptName())
                .deptDesc(request.deptDesc())
                .build();

        Department saved = departmentRepository.save(department);
        return DepartmentResponse.from(saved, 0L);
    }

    @Transactional
    public void updateDepartment(Long deptNo, DepartmentRequest request) {
        Department department = departmentRepository.findById(deptNo)
                .orElseThrow(() -> new EntityNotFoundException("수정할 부서가 없습니다."));

        // 부서명이 바뀌는 경우에만 중복 체크
        if (!department.getDeptName().equals(request.deptName()) &&
            departmentRepository.existsByDeptName(request.deptName())) {
            throw new IllegalArgumentException("이미 존재하는 부서명입니다.");
        }

        department.setDeptName(request.deptName());
        department.setDeptDesc(request.deptDesc());
    }
}