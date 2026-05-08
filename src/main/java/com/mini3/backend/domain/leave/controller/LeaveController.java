package com.mini3.backend.domain.leave.controller;

import com.mini3.backend.domain.leave.dto.LeaveRequestDto;
import com.mini3.backend.domain.leave.dto.LeaveRequestResponse;
import com.mini3.backend.domain.leave.entity.LeaveRequest;
import com.mini3.backend.domain.leave.service.LeaveService;
import com.mini3.backend.global.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> apply(@Valid @RequestBody LeaveRequestDto dto,
                                                       HttpServletRequest httpRequest) {
        LeaveRequest result = leaveService.applyLeave(dto);
        auditService.log("LEAVE_APPLY", dto.getEmpNo(), result.getLeaveId(),
                dto.getLeaveType() + " " + dto.getStartDate() + "~" + dto.getEndDate(),
                getClientIp(httpRequest));
        return ResponseEntity.ok(LeaveRequestResponse.from(result));
    }

    @PatchMapping("/{leaveId}/approve")
    public ResponseEntity<LeaveRequestResponse> approve(@PathVariable Long leaveId,
                                                         @RequestParam Long approverEmpNo,
                                                         HttpServletRequest httpRequest) {
        LeaveRequest result = leaveService.approve(leaveId, approverEmpNo);
        auditService.log("LEAVE_APPROVE", approverEmpNo, leaveId,
                "상태: " + result.getLeaveStatus(), getClientIp(httpRequest));
        return ResponseEntity.ok(LeaveRequestResponse.from(result));
    }

    @PatchMapping("/{leaveId}/reject")
    public ResponseEntity<LeaveRequestResponse> reject(@PathVariable Long leaveId,
                                                        @RequestParam Long approverEmpNo,
                                                        HttpServletRequest httpRequest) {
        LeaveRequest result = leaveService.reject(leaveId, approverEmpNo);
        auditService.log("LEAVE_REJECT", approverEmpNo, leaveId,
                "반려 처리", getClientIp(httpRequest));
        return ResponseEntity.ok(LeaveRequestResponse.from(result));
    }

    @PatchMapping("/{leaveId}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancel(@PathVariable Long leaveId,
                                                        @RequestParam Long empNo,
                                                        HttpServletRequest httpRequest) {
        LeaveRequest result = leaveService.cancel(leaveId, empNo);
        auditService.log("LEAVE_CANCEL", empNo, leaveId,
                "신청 취소", getClientIp(httpRequest));
        return ResponseEntity.ok(LeaveRequestResponse.from(result));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
