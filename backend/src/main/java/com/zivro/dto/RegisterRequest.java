package com.zivro.dto;

import com.zivro.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank @Size(max = 120)
    private String name;

    @NotBlank @Email @Size(max = 180)
    private String email;

    @NotBlank @Size(min = 8, max = 72)
    private String password;

    @NotNull
    private Role role;

    @Size(max = 32)
    private String phone;

    @Size(max = 500)
    private String address;

    /** Required when role is WORKER — primary service category label. */
    @Size(max = 120)
    private String workerCategory;
}
