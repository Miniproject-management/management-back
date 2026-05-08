package com.mini3.backend.global.exception;

import java.math.BigDecimal;

public class InsufficientLeaveException extends RuntimeException {

    private final BigDecimal remainingLeave;
    private final BigDecimal requestedDays;

    public InsufficientLeaveException(BigDecimal remainingLeave, BigDecimal requestedDays) {
        super("남은 연차가 부족합니다. (잔여: " + remainingLeave + "일, 신청: " + requestedDays + "일)");
        this.remainingLeave = remainingLeave;
        this.requestedDays = requestedDays;
    }

    public BigDecimal getRemainingLeave() {
        return remainingLeave;
    }

    public BigDecimal getRequestedDays() {
        return requestedDays;
    }
}
