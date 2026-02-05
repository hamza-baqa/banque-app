package com.banque.eurobank.controller;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Contrôleur pour les virements bancaires
 */
@RestController
@RequestMapping("/api/v1/virements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Virements", description = "Gestion des virements bancaires")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class VirementController {

    private final VirementService virementService;

    @PostMapping
    @Operation(summary = "Effectuer un virement")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> effectuerVirement(
            @Valid @RequestBody VirementDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Demande de virement: {} -> {} montant: {}",
                request.getIbanEmetteur(), request.getIbanBeneficiaire(), request.getMontant());

        // Récupération de l'ID utilisateur depuis le contexte de sécurité
        Long utilisateurId = 1L; // À récupérer du UserDetails en production

        TransactionDTO transaction = virementService.executerVirement(request, utilisateurId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "Virement effectué avec succès"));
    }

    @PostMapping("/instantane")
    @Operation(summary = "Effectuer un virement instantané")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> effectuerVirementInstantane(
            @Valid @RequestBody VirementDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        request.setInstantane(true);
        log.info("Demande de virement instantané: {} -> {} montant: {}",
                request.getIbanEmetteur(), request.getIbanBeneficiaire(), request.getMontant());

        Long utilisateurId = 1L;
        TransactionDTO transaction = virementService.executerVirement(request, utilisateurId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "Virement instantané effectué avec succès"));
    }
}
