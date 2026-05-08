package com.mini3.backend.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class LeaderDashboardResponse {

    private UserDashboardResponse.LeaveBalanceSummary myLeaveBalance;
    private List<UserDashboardResponse.LeaveRequestItem> myRequests;
    private List<TeamMemberStatus> teamOnLeaveToday;
    private List<TeamLeaveUsage> teamLeaveUsage;
    private List<TeamCalendarItem> teamCalendar;

    @Getter
    @Builder
    public static class TeamMemberStatus {
        private Long empNo;
        private String empName;
        private String leaveType;
    }

    @Getter
    @Builder
    public static class TeamLeaveUsage {
        private Long empNo;
        private String empName;
        private BigDecimal totalLeave;
        private BigDecimal usedLeave;
        private BigDecimal remainingLeave;
        private BigDecimal usageRate;
    }

    @Getter
    @Builder
    public static class TeamCalendarItem {
        private String empName;
        private String startDate;
        private String endDate;
        private String leaveType;
    }
}
