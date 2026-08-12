package com.zivro.service;

import com.zivro.domain.Role;
import com.zivro.domain.User;
import com.zivro.domain.Worker;
import com.zivro.dto.AuthResponse;
import com.zivro.dto.LoginRequest;
import com.zivro.dto.RegisterRequest;
import com.zivro.dto.UserResponse;
import com.zivro.exception.BadRequestException;
import com.zivro.exception.ConflictException;
import com.zivro.repository.UserRepository;
import com.zivro.repository.WorkerRepository;
import com.zivro.security.JwtService;
import com.zivro.util.UserMapper;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Self-service registration as ADMIN is not allowed.");
        }
        if (request.getRole() == Role.WORKER
                && !StringUtils.hasText(request.getWorkerCategory())) {
            throw new BadRequestException("workerCategory is required for worker registration.");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email is already registered.");
        }

        User user =
                User.builder()
                        .name(request.getName())
                        .email(request.getEmail().trim().toLowerCase())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(request.getRole())
                        .phone(request.getPhone())
                        .address(request.getAddress())
                        .build();
        user = userRepository.save(user);

        if (request.getRole() == Role.WORKER) {
            Worker worker =
                    Worker.builder()
                            .user(user)
                            .category(request.getWorkerCategory().trim())
                            .employeeId(nextEmployeeId())
                            .build();
            worker = workerRepository.save(worker);
            user.setWorkerProfile(worker);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword()));
        User user =
                userRepository
                        .findByEmailIgnoreCaseWithWorker(request.getEmail().trim())
                        .orElseThrow(() -> new BadRequestException("User not found."));
        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        User user =
                userRepository
                        .findByEmailIgnoreCaseWithWorker(email)
                        .orElseThrow(() -> new BadRequestException("User not found."));
        return UserMapper.toResponse(user);
    }

    private String nextEmployeeId() {
        String candidate;
        do {
            candidate = "ZIV-" + Long.toUnsignedString(secureRandom.nextLong(), 36).toUpperCase();
        } while (workerRepository.existsByEmployeeId(candidate));
        return candidate;
    }

    @Transactional
    public AuthResponse googleLogin(String googleToken, String googleClientId, Role requestedRole, String workerCategory) {
        try {
            com.google.api.client.http.HttpTransport transport = new com.google.api.client.http.javanet.NetHttpTransport();
            com.google.api.client.json.JsonFactory jsonFactory = new com.google.api.client.json.gson.GsonFactory();
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = 
                new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(java.util.Collections.singletonList(googleClientId))
                .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                throw new BadRequestException("Invalid Google token.");
            }

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();

            User user = userRepository.findByEmailIgnoreCaseWithWorker(email).orElse(null);

            if (user == null) {
                Role finalRole = (requestedRole != null) ? requestedRole : Role.USER;
                if (finalRole == Role.ADMIN) {
                    throw new BadRequestException("Self-service registration as ADMIN is not allowed.");
                }
                if (finalRole == Role.WORKER && !StringUtils.hasText(workerCategory)) {
                    throw new BadRequestException("workerCategory is required for worker registration.");
                }

                user = User.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString())) // dummy password
                        .role(finalRole)
                        .authProvider(com.zivro.domain.AuthProvider.GOOGLE)
                        .googleId(googleId)
                        .build();
                user = userRepository.save(user);

                if (finalRole == Role.WORKER) {
                    Worker worker = Worker.builder()
                            .user(user)
                            .category(workerCategory.trim())
                            .employeeId(nextEmployeeId())
                            .build();
                    worker = workerRepository.save(worker);
                    user.setWorkerProfile(worker);
                }
            } else if (user.getAuthProvider() == com.zivro.domain.AuthProvider.LOCAL) {
                // Link account
                user.setAuthProvider(com.zivro.domain.AuthProvider.GOOGLE);
                user.setGoogleId(googleId);
                user = userRepository.save(user);
            }

            String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .user(UserMapper.toResponse(user))
                    .build();

        } catch (Exception e) {
            throw new BadRequestException("Failed to verify Google token: " + e.getMessage());
        }
    }
}
