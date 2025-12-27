package com.jys.smartbudget.exception;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.jys.smartbudget.dto.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 낙관적 락 에러 (Optimistic Lock)
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock() {
        ErrorCode ec = ErrorCode.OPTIMISTIC_LOCK_ERROR;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    // 중복 키 에러 (Duplicate Key)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey() {
        ErrorCode ec = ErrorCode.DUPLICATE_BUDGET;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    // @RequestBody로 받는 객체(DTO) 내부의 필드들 검증 시 사용
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException e) {
        ErrorCode ec = ErrorCode.INVALID_INPUT_VALUE;
        
        List<Map<String, String>> errorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(err -> Map.of(
                    "field", err.getField(),
                    "message", err.getDefaultMessage()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage(), errorDetails));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ErrorCode ec = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    /**
     * @PathVariable 이나 @RequestParam 에 직접 설정한 제약조건 위반 시 (@Min, @Max 등)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        ErrorCode ec = ErrorCode.INVALID_INPUT_VALUE; 
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleLoginError(BadCredentialsException e) {
        ErrorCode ec = ErrorCode.LOGIN_FAILED;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }
}