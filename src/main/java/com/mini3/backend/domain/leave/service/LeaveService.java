package com.mini3.backend.domain.leave.service;

import com.mini3.backend.domain.employee.entity.Employee;
import com.mini3.backend.domain.employee.enums.Position;
import com.mini3.backend.domain.employee.repository.EmployeeRepository;
import com.mini3.backend.domain.leave.dto.LeaveRequestDto;
import com.mini3.backend.domain.leave.entity.LeaveBalance;
import com.mini3.backend.domain.leave.entity.LeaveRequest;
import com.mini3.backend.domain.leave.enums.LeaveStatus;
import com.mini3.backend.domain.leave.repository.LeaveBalanceRepository;
import com.mini3.backend.domain.leave.repository.LeaveRequestRepository;
import com.mini3.backend.global.exception.InsufficientLeaveException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequest applyLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmpNo())
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("종료일이 시작일보다 앞설 수 없습니다.");
        }

        long daySpan = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        if (daySpan > 30) {
            throw new IllegalArgumentException("1회 최대 30일까지만 신청할 수 있습니다.");
        }

        if (dto.getReason() != null) {
            dto.setReason(sanitize(dto.getReason()));
        }

        // 서버에서 시작일/종료일 기준으로 신청 일수 재계산
        BigDecimal serverCalculatedDays =
                calculateBusinessDays(dto.getStartDate(), dto.getEndDate(), dto.getLeaveType());

        // 클라이언트 requestDays 검증
        BigDecimal clientRequestDays = dto.getRequestDays();

        if (clientRequestDays.compareTo(serverCalculatedDays) != 0) {
            throw new IllegalArgumentException(
                    "신청 일수가 실제 영업일과 일치하지 않습니다. (서버 계산: "
                            + serverCalculatedDays + "일)");
        }

        // 연차 조회
        int year = dto.getStartDate().getYear();

        LeaveBalance balance =
                leaveBalanceRepository.findByEmployee_EmpNoAndYear(dto.getEmpNo(), year)
                        .orElseThrow(() -> new IllegalArgumentException("연차 정보가 없습니다."));

        // 잔여 연차 검증
        if (balance.getRemainingLeave().compareTo(serverCalculatedDays) < 0) {
            throw new InsufficientLeaveException(
                    balance.getRemainingLeave(),
                    serverCalculatedDays
            );
        }

        // 중복 신청 검증
        List<LeaveRequest> overlapping =
                leaveRequestRepository.findOverlapping(
                        dto.getEmpNo(),
                        dto.getStartDate(),
                        dto.getEndDate()
                );

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("이미 해당 기간에 신청된 휴가가 있습니다.");
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(dto.getLeaveType())
                .leaveDays(serverCalculatedDays)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .accrualRule("매년 1월 1일 기준")
                .leaveStatus(LeaveStatus.PENDING_MANAGER)
                .isActive("Y")
                .build();

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest approve(Long leaveId, Long approverEmpNo) {

        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("휴가 신청을 찾을 수 없습니다."));

        Employee approver = employeeRepository.findById(approverEmpNo)
                .orElseThrow(() -> new IllegalArgumentException("승인자를 찾을 수 없습니다."));

        if (request.getLeaveStatus() == LeaveStatus.PENDING_MANAGER) {

            // 권한 검사
            if (approver.getPosition() != Position.팀장) {
                throw new IllegalArgumentException("팀장만 승인 가능합니다.");
            }

            if (!approver.getDepartment().getDeptNo()
                    .equals(request.getEmployee().getDepartment().getDeptNo())) {

                throw new IllegalArgumentException("같은 부서만 승인 가능합니다.");
            }

            request.setLeaveStatus(LeaveStatus.PENDING_HR);
            request.setApprovedBy(approver);

        } else if (request.getLeaveStatus() == LeaveStatus.PENDING_HR) {

            if (approver.getPosition() != Position.관리자) {
                throw new IllegalArgumentException("관리자만 승인 가능합니다.");
            }

            request.setLeaveStatus(LeaveStatus.APPROVED);
            request.setApprovedBy(approver);

            deductLeaveBalance(request);

        } else {
            throw new IllegalArgumentException(
                    "승인할 수 없는 상태입니다: " + request.getLeaveStatus()
            );
        }

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest reject(Long leaveId, Long approverEmpNo) {

        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("휴가 신청을 찾을 수 없습니다."));

        Employee approver = employeeRepository.findById(approverEmpNo)
                .orElseThrow(() -> new IllegalArgumentException("승인자를 찾을 수 없습니다."));

        if (request.getLeaveStatus() == LeaveStatus.PENDING_MANAGER) {

            if (approver.getPosition() != Position.팀장) {
                throw new IllegalArgumentException("팀장만 반려 가능합니다.");
            }

            if (!approver.getDepartment().getDeptNo()
                    .equals(request.getEmployee().getDepartment().getDeptNo())) {

                throw new IllegalArgumentException("같은 부서만 반려 가능합니다.");
            }

        } else if (request.getLeaveStatus() == LeaveStatus.PENDING_HR) {

            if (approver.getPosition() != Position.관리자) {
                throw new IllegalArgumentException("관리자만 반려 가능합니다.");
            }

        } else {

            throw new IllegalArgumentException(
                    "반려할 수 없는 상태입니다: " + request.getLeaveStatus()
            );
        }
        request.setLeaveStatus(LeaveStatus.REJECTED);
        request.setApprovedBy(approver);

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest cancel(Long leaveId, Long empNo) {

        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("휴가 신청을 찾을 수 없습니다."));

        if (!request.getEmployee().getEmpNo().equals(empNo)) {
            throw new IllegalArgumentException("본인의 신청만 취소할 수 있습니다.");
        }

        if (request.getLeaveStatus() == LeaveStatus.APPROVED) {
            throw new IllegalArgumentException("이미 승인된 휴가는 취소할 수 없습니다.");
        }

        request.setLeaveStatus(LeaveStatus.CANCELED);
        request.setIsActive("N");

        return leaveRequestRepository.save(request);
    }

    public LeaveRequest getById(Long leaveId) {

        return leaveRequestRepository.findDetailById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("휴가 신청을 찾을 수 없습니다."));
    }

    public List<LeaveRequest> getMyRequests(Long empNo) {

        return leaveRequestRepository.findByEmployee_EmpNoAndIsActive(empNo, "Y");
    }

    public List<LeaveRequest> getPendingForApprover(Long approverEmpNo) {

        Employee approver = employeeRepository.findById(approverEmpNo)
                .orElseThrow(() -> new IllegalArgumentException("승인자를 찾을 수 없습니다."));

        Position position = approver.getPosition();
        
        if (position == null) {
            return List.of();
        }

        // 관리자 → 인사 승인
        if (position == Position.관리자) {
            return leaveRequestRepository.findPendingByStatus(
                    LeaveStatus.PENDING_HR
            );
        }

        // 팀장 → 팀원 승인
        if (position == Position.팀장) {

            Long deptNo = approver.getDepartment().getDeptNo();

            return leaveRequestRepository.findPendingByDept(
                    deptNo,
                    LeaveStatus.PENDING_MANAGER
            );
        }

        return List.of();
    }

    public BigDecimal calculateBusinessDays(
            LocalDate start,
            LocalDate end,
            String leaveType
    ) {

        if (leaveType.contains("반차")) {
            return new BigDecimal("0.5");
        }

        BigDecimal count = BigDecimal.ZERO;

        LocalDate date = start;

        while (!date.isAfter(end)) {

            DayOfWeek day = date.getDayOfWeek();

            if (day != DayOfWeek.SATURDAY
                    && day != DayOfWeek.SUNDAY) {

                count = count.add(BigDecimal.ONE);
            }

            date = date.plusDays(1);
        }

        return count;
    }

    private String sanitize(String input) {

        return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#39;");
    }

    private void deductLeaveBalance(LeaveRequest request) {

        int year = request.getStartDate().getYear();

        LeaveBalance balance =
                leaveBalanceRepository.findByEmployee_EmpNoAndYear(
                                request.getEmployee().getEmpNo(),
                                year
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException("연차 정보가 없습니다.")
                        );

        balance.deduct(request.getLeaveDays());

        leaveBalanceRepository.save(balance);
    }
}