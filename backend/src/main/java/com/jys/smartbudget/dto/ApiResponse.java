package com.jys.smartbudget.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
    private boolean success;        //호출 성공여부
    private String message;         //메세지
    private Object data;            //응답 데이터

    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
