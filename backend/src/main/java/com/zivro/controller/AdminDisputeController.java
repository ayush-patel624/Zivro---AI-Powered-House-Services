package com.zivro.controller;

import com.zivro.dto.AdminDisputeUpdateRequest;
import com.zivro.dto.DisputeResponse;
import com.zivro.service.DisputeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    public List<DisputeResponse> list() {
        return disputeService.listAll();
    }

    @PatchMapping("/{id:\\d+}")
    public DisputeResponse update(@PathVariable Long id, @Valid @RequestBody AdminDisputeUpdateRequest request) {
        return disputeService.adminUpdate(id, request);
    }
}
