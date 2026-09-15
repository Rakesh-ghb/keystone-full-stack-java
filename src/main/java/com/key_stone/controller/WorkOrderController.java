package com.key_stone.controller;

import com.key_stone.domain.User;
import com.key_stone.dto.ApiDtos.AssignRequest;
import com.key_stone.dto.ApiDtos.PartResponse;
import com.key_stone.dto.ApiDtos.PartRequest;
import com.key_stone.dto.ApiDtos.PartUsageRequest;
import com.key_stone.dto.ApiDtos.StatusRequest;
import com.key_stone.dto.ApiDtos.TimeLogRequest;
import com.key_stone.dto.ApiDtos.WorkOrderRequest;
import com.key_stone.dto.ApiDtos.WorkOrderResponse;
import com.key_stone.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService service;

    public WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkOrderResponse> all(
            @RequestParam(required = false) String q,
            Principal p) {

        return service.all(q, null, p.getName());
    }

    @GetMapping("/{id}")
    public WorkOrderResponse get(
            @PathVariable Long id,
            Principal p) {

        return service.get(id, p.getName());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER','ADMIN','CUSTOMER')")
    public WorkOrderResponse create(
            @Valid @RequestBody WorkOrderRequest r) {

        return service.create(r);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER','ADMIN')")
    public WorkOrderResponse update(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderRequest r,
            Principal p) {

        return service.update(id, r, p.getName());
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DISPATCHER','ADMIN')")
    public WorkOrderResponse assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignRequest r,
            Principal p) {

        return service.assign(id, r, p.getName());
    }

    @PostMapping("/{id}/status")
    public WorkOrderResponse status(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest r,
            Principal p) {

        return service.status(id, r, p.getName());
    }

    @PostMapping("/{id}/parts")
    public WorkOrderResponse parts(
            @PathVariable Long id,
            @Valid @RequestBody PartUsageRequest r,
            Principal p) {

        return service.addPart(id, r, p.getName());
    }

    @PostMapping("/{id}/time")
    public WorkOrderResponse time(
            @PathVariable Long id,
            @Valid @RequestBody TimeLogRequest r,
            Principal p) {

        return service.addTime(id, r, p.getName());
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('DISPATCHER','ADMIN')")
    public List<User> technicians() {

        return service.technicians();
    }

    @GetMapping("/parts")
    public List<PartResponse> parts() {

        return service.parts();
    }

    @PostMapping("/parts")
    @PreAuthorize("hasRole('ADMIN')")
    public PartResponse createPart(
            @Valid @RequestBody PartRequest r) {

        return service.createPart(r);
    }
}