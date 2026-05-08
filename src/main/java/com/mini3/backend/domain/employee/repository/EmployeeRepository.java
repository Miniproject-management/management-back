package com.mini3.backend.domain.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mini3.backend.domain.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 부서별 직원 조회
    List<Employee> findByDepartmentDeptNo(Long deptNo);

    // 이름 검색
    List<Employee> findByEmpNameContaining(String empName);
}
