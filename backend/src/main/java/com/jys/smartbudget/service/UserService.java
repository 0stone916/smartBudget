package com.jys.smartbudget.service;

import com.jys.smartbudget.dto.UserDTO;
import com.jys.smartbudget.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;  // SecurityConfig.java에서 생성자 주입으로 변경

    public UserDTO login(String userId, String password) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public void register(UserDTO user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insertUser(user);
    }
}
