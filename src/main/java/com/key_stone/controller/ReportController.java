package com.key_stone.controller;

import com.key_stone.dto.ApiDtos.ReportSummary;
import com.key_stone.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ReportSummary summary() {
        return service.summary();
    }
}