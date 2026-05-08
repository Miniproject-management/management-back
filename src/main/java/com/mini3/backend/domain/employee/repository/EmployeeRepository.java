package com.mini3.backend.domain.employee.repository;

import com.mini3.backend.domain.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
