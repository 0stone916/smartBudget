package com.jys.smartbudget.mapper;

import com.jys.smartbudget.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    // 로그인용: user_id로 사용자 조회
    UserDTO findByUserId(@Param("userId") String userId);

    // 회원가입용: 새 유저 등록
    void insertUser(UserDTO user);
}
