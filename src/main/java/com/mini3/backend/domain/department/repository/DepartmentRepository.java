package com.mini3.backend.domain.department.repository;

import com.mini3.backend.domain.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    // 1. 기존 코드: 부서명 중복 확인용 (T/F 반환)
    boolean existsByDeptName(String deptName);

    // 2. 추가 추천: 부서명으로 부서 엔티티 전체를 조회할 때 사용
    Optional<Department> findByDeptName(String deptName);
}