package com.mini3.backend.domain.leave.dto;

import com.mini3.backend.domain.leave.entity.LeaveRequest;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LeaveRequestResponse {

    private Long leaveId;
    private Long empNo;
    private String empName;
    private String leaveType;
    private BigDecimal leaveDays;
    private String startDate;
    private String endDate;
    private String reason;
    private String leaveStatus;

    public static LeaveRequestResponse from(LeaveRequest request) {
        return LeaveRequestResponse.builder()
                .leaveId(request.getLeaveId())
                .empNo(request.getEmployee().getEmpNo())
                .empName(request.getEmployee().getEmpName())
                .leaveType(request.getLeaveType())
                .leaveDays(request.getLeaveDays())
                .startDate(request.getStartDate().toString())
                .endDate(request.getEndDate().toString())
                .reason(request.getReason())
                .leaveStatus(request.getLeaveStatus().name())
                .build();
    }
}
