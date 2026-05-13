package com.mini3.backend.domain.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.enums.Position;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentDeptNo(Long deptNo);

    List<Employee> findByDepartment_DeptNo(Long deptNo);

    List<Employee> findByDepartment_DeptNoAndPosition(Long deptNo, Position position);

    List<Employee> findByEmpNameContaining(String empName);

    @Query("""
        select e
        from Employee e
        join fetch e.department
        where e.empNo = :empNo
    """)
    Optional<Employee> findByEmpNoWithDepartment(@Param("empNo") Long empNo);
}