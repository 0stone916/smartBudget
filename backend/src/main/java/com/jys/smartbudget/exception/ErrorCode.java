package com.jys.smartbudget.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 인증/인가 관련
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "A002", "해당 자원에 대한 권한이 없습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "A003", "인증 정보가 유효하지 않습니다."),

    // 중복 관련
    DUPLICATE_BUDGET(HttpStatus.CONFLICT, "B001", "이미 등록된 예산입니다."),
    OPTIMISTIC_LOCK_ERROR(HttpStatus.CONFLICT, "B002", "이미 다른 요청에 의해 수정되었습니다. 다시 시도해 주세요."),

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}