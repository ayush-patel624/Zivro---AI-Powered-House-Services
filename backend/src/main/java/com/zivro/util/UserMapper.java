package com.zivro.util;

import com.zivro.domain.User;
import com.zivro.domain.Worker;
import com.zivro.dto.UserResponse;
import com.zivro.dto.WorkerSummaryResponse;
import java.util.Optional;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .address(user.getAddress())
                .worker(Optional.ofNullable(user.getWorkerProfile()).map(UserMapper::toWorkerSummary).orElse(null))
                .build();
    }

    private static WorkerSummaryResponse toWorkerSummary(Worker w) {
        return WorkerSummaryResponse.builder()
                .id(w.getId())
                .employeeId(w.getEmployeeId())
                .category(w.getCategory())
                .rating(w.getRating())
                .verified(w.isVerified())
                .depositPaid(w.isDepositPaid())
                .build();
    }
}
