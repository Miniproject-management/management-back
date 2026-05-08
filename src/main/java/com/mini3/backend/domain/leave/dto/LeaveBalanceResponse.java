package com.mini3.backend.domain.leave.dto;

import com.mini3.backend.domain.leave.entity.LeaveBalance;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LeaveBalanceResponse {

    private Long balanceId;
    private Long empNo;
    private String empName;
    private Integer year;
    private BigDecimal totalLeave;
    private BigDecimal usedLeave;
    private BigDecimal remainingLeave;

    public static LeaveBalanceResponse from(LeaveBalance balance) {
        return LeaveBalanceResponse.builder()
                .balanceId(balance.getBalanceId())
                .empNo(balance.getEmployee().getEmpNo())
                .empName(balance.getEmployee().getEmpName())
                .year(balance.getYear())
                .totalLeave(balance.getTotalLeave())
                .usedLeave(balance.getUsedLeave())
                .remainingLeave(balance.getRemainingLeave())
                .build();
    }
}
