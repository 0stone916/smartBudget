package com.jys.smartbudget.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.jys.smartbudget.dto.ReconciliationErrorDTO;


@Mapper
public interface ReconcileLogMapper {
    // 에러 로그 삽입
    void insertErrorLog(ReconciliationErrorDTO errorDto);
}