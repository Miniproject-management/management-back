package com.mini3.backend.domain.leave.controller;

import com.mini3.backend.domain.leave.dto.LeaveBalanceDto;
import com.mini3.backend.domain.leave.dto.LeaveBalanceResponse;
import com.mini3.backend.domain.leave.service.LeaveBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @PostMapping
    public ResponseEntity<LeaveBalanceResponse> createOrUpdate(@Valid @RequestBody LeaveBalanceDto dto) {
        return ResponseEntity.ok(LeaveBalanceResponse.from(leaveBalanceService.createOrUpdate(dto)));
    }

    @GetMapping
    public ResponseEntity<LeaveBalanceResponse> getBalance(@RequestParam Long empNo,
                                                            @RequestParam Integer year) {
        return ResponseEntity.ok(LeaveBalanceResponse.from(leaveBalanceService.getBalance(empNo, year)));
    }
}
