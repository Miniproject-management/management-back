package com.mini3.backend.domain.leave.entity;

import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.leave.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_id")
    private Long leaveId;

    // 신청자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_no", nullable = false)
    private Employee employee;

    @Column(name = "leave_type", nullable = false, length = 50)
    private String leaveType;

    @Column(name = "leave_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal leaveDays;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "accrual_rule", nullable = false, length = 255)
    private String accrualRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_status")
    private LeaveStatus leaveStatus;

    // 마지막 승인자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive;
}
