package com.zivro.controller;

import com.zivro.dto.AuthResponse;
import com.zivro.dto.LoginRequest;
import com.zivro.dto.RegisterRequest;
import com.zivro.dto.UserResponse;
import com.zivro.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserDetails principal) {
        return authService.me(principal.getUsername());
    }

    @PostMapping("/google")
    public AuthResponse googleLogin(
            @Valid @RequestBody com.zivro.dto.GoogleLoginRequest request,
            @org.springframework.beans.factory.annotation.Value("${ZIVRO_GOOGLE_CLIENT_ID:14405976128-hfobk3av965n7jr93ah7h981f2ihhjii.apps.googleusercontent.com}") String googleClientId) {
        return authService.googleLogin(request.getCredential(), googleClientId, request.getRole(), request.getWorkerCategory());
    }
}
