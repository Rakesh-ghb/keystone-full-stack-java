package com.key_stone.service;

import com.key_stone.dto.ApiDtos.LoginRequest;
import com.key_stone.dto.ApiDtos.LoginResponse;
import com.key_stone.repository.UserRepository;
import com.key_stone.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager auth;
    private final UserRepository users;
    private final UserDetailsService details;
    private final JwtService jwt;

    public AuthService(
            AuthenticationManager auth,
            UserRepository users,
            UserDetailsService details,
            JwtService jwt) {
        this.auth = auth;
        this.users = users;
        this.details = details;
        this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest r) {

        auth.authenticate(
                new UsernamePasswordAuthenticationToken(
                        r.email(),
                        r.password()
                )
        );

        var user = users.findByEmailIgnoreCase(r.email())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        String token = jwt.generate(
                details.loadUserByUsername(r.email())
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getRole().name()
        );
    }
}