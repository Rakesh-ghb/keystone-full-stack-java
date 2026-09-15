package com.key_stone.controller;

import com.key_stone.dto.ApiDtos.CustomerRequest;
import com.key_stone.dto.ApiDtos.CustomerResponse;
import com.key_stone.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public List<CustomerResponse> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public CustomerResponse create(
            @Valid @RequestBody CustomerRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest r) {
        return service.update(id, r);
    }
}