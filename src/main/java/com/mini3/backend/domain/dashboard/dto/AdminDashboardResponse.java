package com.mini3.backend.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AdminDashboardResponse {

    private BigDecimal companyAverageUsage;
    private List<DeptLeaveSummary> deptSummaries;
    private List<AllLeaveRequestItem> allRequests;

    @Getter
    @Builder
    public static class DeptLeaveSummary {
        private String deptName;
        private BigDecimal avgTotalLeave;
        private BigDecimal avgUsedLeave;
        private BigDecimal avgRemainingLeave;
    }

    @Getter
    @Builder
    public static class AllLeaveRequestItem {
        private Long leaveId;
        private String empName;
        private String deptName;
        private String leaveType;
        private String startDate;
        private String endDate;
        private BigDecimal leaveDays;
        private String status;
    }
}
