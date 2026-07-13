package com.zivro.dto;

import com.zivro.domain.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {
    Long id;
    String name;
    String email;
    Role role;
    String phone;
    String address;
    WorkerSummaryResponse worker;
}
