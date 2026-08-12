package com.zivro.dto;

import com.zivro.domain.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "Google token is required")
    private String credential;

    private Role role;
    
    private String workerCategory;
}
