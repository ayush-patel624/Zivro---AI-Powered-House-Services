package com.zivro.controller;

import com.zivro.domain.UrgencyLevel;
import com.zivro.dto.PriceQuoteResponse;
import com.zivro.dto.ServiceResponse;
import com.zivro.service.ServiceCatalogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    @GetMapping
    public List<ServiceResponse> list() {
        return serviceCatalogService.listAll();
    }

    @GetMapping("/{id}")
    public ServiceResponse get(@PathVariable Long id) {
        return serviceCatalogService.getById(id);
    }

    @GetMapping("/{id}/quote")
    public PriceQuoteResponse quote(
            @PathVariable Long id, @RequestParam(defaultValue = "NORMAL") UrgencyLevel urgency) {
        return serviceCatalogService.quote(id, urgency);
    }
}
