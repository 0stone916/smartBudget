package com.jys.smartbudget.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String userId;
    private String password;
    private String name;
    private String email;
}
