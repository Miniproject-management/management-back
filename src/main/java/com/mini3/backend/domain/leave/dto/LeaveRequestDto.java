package com.mini3.backend.domain.leave.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequestDto {

    @NotNull(message = "사원 번호는 필수입니다.")
    private Long empNo;

    @NotBlank(message = "휴가 유형은 필수입니다.")
    private String leaveType;

    @NotNull(message = "시작일은 필수입니다.")
    @FutureOrPresent(message = "과거 날짜로 신청할 수 없습니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @FutureOrPresent(message = "과거 날짜로 신청할 수 없습니다.")
    private LocalDate endDate;

    @NotNull(message = "신청 일수는 필수입니다.")
    @Min(value = 1, message = "신청 일수는 1일 이상이어야 합니다.")
    private Integer requestDays;

    @Size(max = 500, message = "사유는 500자 이내로 입력해주세요.")
    private String reason;
}
