package com.mini3.backend.domain.leave.repository;

import com.mini3.backend.domain.leave.entity.LeaveRequest;
import com.mini3.backend.domain.leave.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee_EmpNoAndIsActive(Long empNo, String isActive);

    List<LeaveRequest> findByEmployee_EmpNoAndLeaveStatusAndIsActive(Long empNo, LeaveStatus status, String isActive);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.empNo = :empNo " +
            "AND lr.isActive = 'Y' " +
            "AND lr.leaveStatus NOT IN ('REJECTED', 'CANCELED') " +
            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlapping(@Param("empNo") Long empNo,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee e " +
            "WHERE e.department.deptNo = :deptNo " +
            "AND lr.isActive = 'Y' " +
            "AND lr.leaveStatus = 'APPROVED' " +
            "AND lr.startDate <= :date AND lr.endDate >= :date")
    List<LeaveRequest> findTeamOnLeaveToday(@Param("deptNo") Long deptNo,
                                            @Param("date") LocalDate date);

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee e " +
            "WHERE e.department.deptNo = :deptNo AND lr.isActive = 'Y' " +
            "AND lr.leaveStatus = 'APPROVED'")
    List<LeaveRequest> findApprovedByDept(@Param("deptNo") Long deptNo);

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee e " +
            "WHERE lr.isActive = 'Y'")
    List<LeaveRequest> findAllActive();

    List<LeaveRequest> findByEmployee_EmpNoAndLeaveStatusInAndIsActive(
            Long empNo, List<LeaveStatus> statuses, String isActive);
}
