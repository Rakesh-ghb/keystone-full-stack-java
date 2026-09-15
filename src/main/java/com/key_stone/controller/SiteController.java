package com.key_stone.controller;

import com.key_stone.dto.ApiDtos.SiteRequest;
import com.key_stone.dto.ApiDtos.SiteResponse;
import com.key_stone.service.SiteService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/sites")
public class SiteController {

    private final SiteService service;

    public SiteController(SiteService service) {
        this.service = service;
    }

    @GetMapping
    public List<SiteResponse> all(
            @PathVariable Long customerId) {

        return service.byCustomer(customerId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public SiteResponse create(
            @PathVariable Long customerId,
            @Valid @RequestBody SiteRequest r) {

        if (!customerId.equals(r.customerId())) {
            throw new IllegalArgumentException(
                    "Customer mismatch");
        }

        return service.create(r);
    }
}