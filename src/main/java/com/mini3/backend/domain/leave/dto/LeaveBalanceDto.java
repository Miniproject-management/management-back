package com.mini3.backend.domain.leave.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LeaveBalanceDto {

    @NotNull(message = "사원 번호는 필수입니다.")
    private Long empNo;

    @NotNull(message = "연도는 필수입니다.")
    private Integer year;

    @NotNull(message = "총 연차는 필수입니다.")
    @DecimalMin(value = "0.0", message = "총 연차는 0 이상이어야 합니다.")
    private BigDecimal totalLeave;
}
