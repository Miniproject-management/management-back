package com.mini3.backend.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class UserDashboardResponse {

    private LeaveBalanceSummary leaveBalance;
    private List<LeaveRequestItem> myRequests;
    private List<LeaveScheduleItem> mySchedule;

    @Getter
    @Builder
    public static class LeaveBalanceSummary {
        private BigDecimal totalLeave;
        private BigDecimal usedLeave;
        private BigDecimal remainingLeave;
    }

    @Getter
    @Builder
    public static class LeaveRequestItem {
        private Long leaveId;
        private String leaveType;
        private String startDate;
        private String endDate;
        private BigDecimal leaveDays;
        private String reason;
        private String status;
    }

    @Getter
    @Builder
    public static class LeaveScheduleItem {
        private String startDate;
        private String endDate;
        private String leaveType;
        private BigDecimal leaveDays;
    }
}
