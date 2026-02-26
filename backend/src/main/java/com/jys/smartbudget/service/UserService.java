package com.jys.smartbudget.service;

import com.jys.smartbudget.dto.UserDTO;
import com.jys.smartbudget.mapper.UserMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;  // SecurityConfig.java에서 생성자 주입으로 변경
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");


    public UserDTO login(String userId, String password) {
                  log.info("@!!!!!!!!!!!!!!!!!");
        UserDTO user = userMapper.findByUserId(userId);
                log.info("@@@@@@@3333@@@");

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                log.info("@@@@@@@444@@@");

            return user;
        }
                log.info("@@@@@@@555@@@");

        return null;
    }

    public void register(UserDTO user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insertUser(user);
    }

        @Transactional
    public void changeAutoBudgetPolicy(Boolean autoEnabled, String userId) {
        userMapper.changeAutoBudgetPolicy(
            autoEnabled,
            userId,
            userId // updated_by
        );

            auditLog.info(
            "POLICY_CHANGE policy=AUTO_BUDGET_MONTHLY user={} after_auto_enabled={}",
            userId,
            autoEnabled
        );
    }
}
