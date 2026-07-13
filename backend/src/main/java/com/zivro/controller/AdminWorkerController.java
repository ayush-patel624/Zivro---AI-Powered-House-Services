package com.zivro.controller;

import com.zivro.dto.AdminWorkerRowResponse;
import com.zivro.dto.WorkerVerificationRequest;
import com.zivro.service.AdminWorkerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/workers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWorkerController {

    private final AdminWorkerService adminWorkerService;

    @GetMapping
    public List<AdminWorkerRowResponse> list() {
        return adminWorkerService.listWorkers();
    }

    @PatchMapping("/{id:\\d+}/verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setVerification(
            @PathVariable Long id, @Valid @RequestBody WorkerVerificationRequest request) {
        adminWorkerService.setVerified(id, Boolean.TRUE.equals(request.getVerified()));
    }
}
