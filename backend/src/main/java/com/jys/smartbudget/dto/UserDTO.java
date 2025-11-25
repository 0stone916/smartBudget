package com.jys.smartbudget.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String userId;
    private String password;
    private String name;
    private String email;
}
