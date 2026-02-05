package com.banque.eurobank.controller;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Contrôleur d'authentification
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Gestion de l'authentification")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        log.info("Tentative de connexion: {}", request.getLogin());
        LoginResponseDTO response = authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Connexion réussie"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        log.info("Rafraîchissement du token");
        LoginResponseDTO response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Déconnexion: {}", userDetails.getUsername());
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Déconnexion réussie"));
    }
}
