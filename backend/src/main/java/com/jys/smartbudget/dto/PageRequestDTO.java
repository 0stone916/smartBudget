package com.jys.smartbudget.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDTO {
    private int page = 0;   // 기본 페이지 0
    private int size = 20;  // 기본 사이즈 20

    public int getOffset() {
        return page * size;
    }
}
