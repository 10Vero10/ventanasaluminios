package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.ChangePasswordRequest;
import com.ventanasyaluminios.dto.request.LoginRequest;
import com.ventanasyaluminios.dto.response.AdminUserResponse;
import com.ventanasyaluminios.security.AuthenticatedAdmin;
import com.ventanasyaluminios.security.SessionTokens;
import com.ventanasyaluminios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sesión del panel. El JWT viaja solo en la cookie httpOnly; el cuerpo nunca lo incluye. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;
    private final SessionTokens sessionTokens;

    public AuthController(AuthService service, SessionTokens sessionTokens) {
        this.service = service;
        this.sessionTokens = sessionTokens;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminUserResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthSession session = service.login(request);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, session.sessionCookie()).body(session.admin());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedAdmin admin) {
        service.logout(admin.id());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, sessionTokens.clearCookie()).build();
    }

    @GetMapping("/me")
    public AdminUserResponse me(@AuthenticationPrincipal AuthenticatedAdmin admin) {
        return service.me(admin.id());
    }

    @PostMapping("/change-password")
    public ResponseEntity<AdminUserResponse> changePassword(@AuthenticationPrincipal AuthenticatedAdmin admin,
                                                            @Valid @RequestBody ChangePasswordRequest request) {
        AuthService.AuthSession session = service.changePassword(admin.id(), request);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, session.sessionCookie()).body(session.admin());
    }
}
