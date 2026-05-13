package com.mini3.backend.global;

import com.mini3.backend.global.exception.InsufficientLeaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientLeaveException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientLeave(InsufficientLeaveException e) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", e.getMessage(),
                        "remainingLeave", e.getRemainingLeave(),
                        "requestedDays", e.getRequestedDays()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest()
                .body(Map.of("error", message));
    }

    /**
     * {@link ResponseStatusException} 은 {@link Exception} 의 하위 타입이라,
     * 전용 처리 없이 {@code Exception} 핸들러에 걸리면 원래 HTTP 상태·메시지가 사라지고 500 고정 문구만 내려감
     * (예: 이력서 PDF S3/프록시 실패 시 원인 파악 불가).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
        HttpStatusCode code = e.getStatusCode();
        HttpStatus status = code instanceof HttpStatus hs
                ? hs
                : HttpStatus.resolve(code.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String reason = e.getReason();
        if (reason == null || reason.isBlank()) {
            reason = status.getReasonPhrase();
        }
        if (status.is5xxServerError()) {
            log.error("ResponseStatusException status={} reason={}", status.value(), reason, e);
        } else {
            log.warn("ResponseStatusException status={} reason={}", status.value(), reason);
        }
        return ResponseEntity.status(status).body(Map.of("error", reason));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 오류가 발생했습니다. 관리자에게 문의해주세요."));
    }
}
