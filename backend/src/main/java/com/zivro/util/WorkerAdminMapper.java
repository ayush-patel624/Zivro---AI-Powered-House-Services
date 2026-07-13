package com.zivro.util;

import com.zivro.domain.Worker;
import com.zivro.dto.AdminWorkerRowResponse;

public final class WorkerAdminMapper {

    private WorkerAdminMapper() {}

    public static AdminWorkerRowResponse toAdminRow(Worker w) {
        return AdminWorkerRowResponse.builder()
                .id(w.getId())
                .employeeId(w.getEmployeeId())
                .verified(w.isVerified())
                .userName(w.getUser().getName())
                .userEmail(w.getUser().getEmail())
                .build();
    }
}
