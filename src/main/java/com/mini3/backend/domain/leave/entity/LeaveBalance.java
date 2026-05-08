package com.mini3.backend.domain.leave.entity;

import com.mini3.backend.domain.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_no", nullable = false)
    private Employee employee;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_leave", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalLeave;

    @Column(name = "used_leave", nullable = false, precision = 5, scale = 2)
    private BigDecimal usedLeave;

    public BigDecimal getRemainingLeave() {
        return totalLeave.subtract(usedLeave);
    }

    public void deduct(BigDecimal days) {
        this.usedLeave = this.usedLeave.add(days);
    }
}
