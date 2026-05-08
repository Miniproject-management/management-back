package com.mini3.backend.domain.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mini3.backend.domain.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartmentDeptNo(Long deptNo);

    List<Employee> findByDepartment_DeptNo(Long deptNo);

    List<Employee> findByEmpNameContaining(String empName);
}
