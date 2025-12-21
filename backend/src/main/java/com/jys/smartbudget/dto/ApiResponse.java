package com.jys.smartbudget.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * 공통 API 응답 규격 클래스
 * 제네릭을 사용하여 다양한 데이터 타입을 일관된 형식으로 반환
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답 생성 (정적 팩토리 메서드)
     * static 메서드 내 제네릭 타입 활용을 위해 메서드 레벨의 <T> 선언
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // 실패 응답
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
