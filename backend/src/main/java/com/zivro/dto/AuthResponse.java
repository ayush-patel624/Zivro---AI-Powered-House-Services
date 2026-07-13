package com.zivro.dto;

import com.zivro.domain.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String accessToken;
    String tokenType;
    UserResponse user;
}
