package com.mini3.backend.domain.leave.repository;

import com.mini3.backend.domain.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    @Query("""
        SELECT lb
        FROM LeaveBalance lb
        JOIN FETCH lb.employee e
        JOIN FETCH e.department
        WHERE e.empNo = :empNo
        AND lb.year = :year
        """)
    Optional<LeaveBalance> findDetailByEmployeeAndYear(
            @Param("empNo") Long empNo,
            @Param("year") Integer year
    );

    List<LeaveBalance> findByYear(Integer year);

    @Query("SELECT lb FROM LeaveBalance lb JOIN FETCH lb.employee e " +
            "WHERE e.department.deptNo = :deptNo AND lb.year = :year")
    List<LeaveBalance> findByDeptAndYear(@Param("deptNo") Long deptNo, @Param("year") Integer year);
}
