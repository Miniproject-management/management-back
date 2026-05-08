package com.mini3.backend.domain.department.repository;

import com.mini3.backend.domain.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // 기본 CRUD 외에 필요한 부서명 중복 확인 등 추가 가능
    boolean existsByDeptName(String deptName);
}