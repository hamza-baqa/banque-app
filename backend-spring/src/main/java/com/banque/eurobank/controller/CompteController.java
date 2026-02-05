package com.banque.eurobank.controller;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Contrôleur pour la gestion des comptes bancaires
 */
@RestController
@RequestMapping("/api/v1/comptes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comptes", description = "Gestion des comptes bancaires")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class CompteController {

    private final CompteService compteService;

    @GetMapping("/{iban}")
    @Operation(summary = "Récupérer un compte par IBAN")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<CompteDTO>> getCompte(
            @Parameter(description = "IBAN du compte") @PathVariable String iban) {
        log.info("Récupération du compte: {}", iban);
        CompteDTO compte = compteService.getCompteByIban(iban);
        return ResponseEntity.ok(ApiResponse.success(compte));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Récupérer les comptes d'un client")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<List<CompteResumeDTO>>> getComptesByClient(
            @Parameter(description = "ID du client") @PathVariable Long clientId) {
        log.info("Récupération des comptes du client: {}", clientId);
        List<CompteResumeDTO> comptes = compteService.getComptesByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(comptes));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau compte")
    @PreAuthorize("hasAnyRole('CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<CompteDTO>> creerCompte(
            @Valid @RequestBody CompteCreationDTO request) {
        log.info("Création d'un nouveau compte pour client: {}", request.getClientId());
        CompteDTO compte = compteService.creerCompte(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(compte, "Compte créé avec succès"));
    }
}
