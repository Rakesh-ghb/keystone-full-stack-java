package com.key_stone.controller;

import com.key_stone.dto.ApiDtos.LoginRequest;
import com.key_stone.dto.ApiDtos.LoginResponse;
import com.key_stone.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest r) {

        return service.login(r);
    }
}