package com.mini3.backend.domain.dashboard.service;

import com.mini3.backend.domain.dashboard.dto.AdminDashboardResponse;
import com.mini3.backend.domain.dashboard.dto.LeaderDashboardResponse;
import com.mini3.backend.domain.dashboard.dto.UserDashboardResponse;
import com.mini3.backend.domain.department.entity.Department;
import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import com.mini3.backend.domain.leave.entity.LeaveBalance;
import com.mini3.backend.domain.leave.entity.LeaveRequest;
import com.mini3.backend.domain.leave.enums.LeaveStatus;
import com.mini3.backend.domain.leave.repository.LeaveBalanceRepository;
import com.mini3.backend.domain.leave.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    public UserDashboardResponse getUserDashboard(Long empNo) {
        int year = LocalDate.now().getYear();

        LeaveBalance balance = leaveBalanceRepository.findByEmployee_EmpNoAndYear(empNo, year)
                .orElse(null);

        UserDashboardResponse.LeaveBalanceSummary balanceSummary = balance != null
                ? UserDashboardResponse.LeaveBalanceSummary.builder()
                    .totalLeave(balance.getTotalLeave())
                    .usedLeave(balance.getUsedLeave())
                    .remainingLeave(balance.getRemainingLeave())
                    .build()
                : UserDashboardResponse.LeaveBalanceSummary.builder()
                    .totalLeave(BigDecimal.ZERO)
                    .usedLeave(BigDecimal.ZERO)
                    .remainingLeave(BigDecimal.ZERO)
                    .build();

        List<LeaveRequest> requests = leaveRequestRepository
                .findByEmployee_EmpNoAndIsActive(empNo, "Y");

        List<UserDashboardResponse.LeaveRequestItem> requestItems = requests.stream()
                .map(r -> UserDashboardResponse.LeaveRequestItem.builder()
                        .leaveId(r.getLeaveId())
                        .leaveType(r.getLeaveType())
                        .startDate(r.getStartDate().toString())
                        .endDate(r.getEndDate().toString())
                        .leaveDays(r.getLeaveDays())
                        .reason(r.getReason())
                        .status(r.getLeaveStatus().name())
                        .build())
                .toList();

        List<UserDashboardResponse.LeaveScheduleItem> scheduleItems = requests.stream()
                .filter(r -> r.getLeaveStatus() == LeaveStatus.APPROVED)
                .map(r -> UserDashboardResponse.LeaveScheduleItem.builder()
                        .startDate(r.getStartDate().toString())
                        .endDate(r.getEndDate().toString())
                        .leaveType(r.getLeaveType())
                        .leaveDays(r.getLeaveDays())
                        .build())
                .toList();

        return UserDashboardResponse.builder()
                .leaveBalance(balanceSummary)
                .myRequests(requestItems)
                .mySchedule(scheduleItems)
                .build();
    }

    public LeaderDashboardResponse getLeaderDashboard(Long empNo) {
        Employee leader = employeeRepository.findById(empNo)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));

        Long deptNo = leader.getDepartment().getDeptNo();
        int year = LocalDate.now().getYear();

        UserDashboardResponse userDashboard = getUserDashboard(empNo);

        List<LeaveRequest> onLeaveToday = leaveRequestRepository
                .findTeamOnLeaveToday(deptNo, LocalDate.now());

        List<LeaderDashboardResponse.TeamMemberStatus> teamOnLeave = onLeaveToday.stream()
                .map(r -> LeaderDashboardResponse.TeamMemberStatus.builder()
                        .empNo(r.getEmployee().getEmpNo())
                        .empName(r.getEmployee().getEmpName())
                        .leaveType(r.getLeaveType())
                        .build())
                .toList();

        List<LeaveBalance> teamBalances = leaveBalanceRepository.findByDeptAndYear(deptNo, year);

        List<LeaderDashboardResponse.TeamLeaveUsage> teamUsage = teamBalances.stream()
                .map(b -> {
                    BigDecimal usageRate = b.getTotalLeave().compareTo(BigDecimal.ZERO) > 0
                            ? b.getUsedLeave().multiply(new BigDecimal("100"))
                                .divide(b.getTotalLeave(), 1, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return LeaderDashboardResponse.TeamLeaveUsage.builder()
                            .empNo(b.getEmployee().getEmpNo())
                            .empName(b.getEmployee().getEmpName())
                            .totalLeave(b.getTotalLeave())
                            .usedLeave(b.getUsedLeave())
                            .remainingLeave(b.getRemainingLeave())
                            .usageRate(usageRate)
                            .build();
                })
                .toList();

        List<LeaveRequest> approvedTeam = leaveRequestRepository.findApprovedByDept(deptNo);

        List<LeaderDashboardResponse.TeamCalendarItem> teamCalendar = approvedTeam.stream()
                .map(r -> LeaderDashboardResponse.TeamCalendarItem.builder()
                        .empName(r.getEmployee().getEmpName())
                        .startDate(r.getStartDate().toString())
                        .endDate(r.getEndDate().toString())
                        .leaveType(r.getLeaveType())
                        .build())
                .toList();

        return LeaderDashboardResponse.builder()
                .myLeaveBalance(userDashboard.getLeaveBalance())
                .myRequests(userDashboard.getMyRequests())
                .teamOnLeaveToday(teamOnLeave)
                .teamLeaveUsage(teamUsage)
                .teamCalendar(teamCalendar)
                .build();
    }

    public AdminDashboardResponse getAdminDashboard() {
        int year = LocalDate.now().getYear();

        List<LeaveBalance> allBalances = leaveBalanceRepository.findByYear(year);

        BigDecimal companyAvg = BigDecimal.ZERO;
        if (!allBalances.isEmpty()) {
            BigDecimal totalUsed = allBalances.stream()
                    .map(LeaveBalance::getUsedLeave)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            companyAvg = totalUsed.divide(new BigDecimal(allBalances.size()), 1, RoundingMode.HALF_UP);
        }

        Map<Department, List<LeaveBalance>> byDept = allBalances.stream()
                .collect(Collectors.groupingBy(b -> b.getEmployee().getDepartment()));

        List<AdminDashboardResponse.DeptLeaveSummary> deptSummaries = byDept.entrySet().stream()
                .map(entry -> {
                    List<LeaveBalance> balances = entry.getValue();
                    BigDecimal count = new BigDecimal(balances.size());
                    BigDecimal avgTotal = balances.stream()
                            .map(LeaveBalance::getTotalLeave)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(count, 1, RoundingMode.HALF_UP);
                    BigDecimal avgUsed = balances.stream()
                            .map(LeaveBalance::getUsedLeave)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(count, 1, RoundingMode.HALF_UP);
                    return AdminDashboardResponse.DeptLeaveSummary.builder()
                            .deptName(entry.getKey().getDeptName())
                            .avgTotalLeave(avgTotal)
                            .avgUsedLeave(avgUsed)
                            .avgRemainingLeave(avgTotal.subtract(avgUsed))
                            .build();
                })
                .toList();

        List<LeaveRequest> allRequests = leaveRequestRepository.findAllActive();

        List<AdminDashboardResponse.AllLeaveRequestItem> requestItems = allRequests.stream()
                .map(r -> AdminDashboardResponse.AllLeaveRequestItem.builder()
                        .leaveId(r.getLeaveId())
                        .empName(r.getEmployee().getEmpName())
                        .deptName(r.getEmployee().getDepartment().getDeptName())
                        .leaveType(r.getLeaveType())
                        .startDate(r.getStartDate().toString())
                        .endDate(r.getEndDate().toString())
                        .leaveDays(r.getLeaveDays())
                        .status(r.getLeaveStatus().name())
                        .build())
                .toList();

        return AdminDashboardResponse.builder()
                .companyAverageUsage(companyAvg)
                .deptSummaries(deptSummaries)
                .allRequests(requestItems)
                .build();
    }
}
