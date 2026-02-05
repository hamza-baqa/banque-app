package com.banque.eurobank.controller;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Contrôleur pour la gestion des cartes bancaires
 */
@RestController
@RequestMapping("/api/v1/cartes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cartes", description = "Gestion des cartes bancaires")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class CarteController {

    private final CarteService carteService;

    @GetMapping("/compte/{compteId}")
    @Operation(summary = "Récupérer les cartes d'un compte")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<List<CarteDTO>>> getCartesByCompte(
            @Parameter(description = "ID du compte") @PathVariable Long compteId) {
        log.info("Récupération des cartes du compte: {}", compteId);
        List<CarteDTO> cartes = carteService.getCartesByCompte(compteId);
        return ResponseEntity.ok(ApiResponse.success(cartes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une carte par ID")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<CarteDTO>> getCarte(
            @Parameter(description = "ID de la carte") @PathVariable Long id) {
        log.info("Récupération de la carte: {}", id);
        CarteDTO carte = carteService.getCarteById(id);
        return ResponseEntity.ok(ApiResponse.success(carte));
    }

    @PutMapping("/{id}/options")
    @Operation(summary = "Modifier les options d'une carte")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<CarteDTO>> modifierOptions(
            @PathVariable Long id,
            @Valid @RequestBody CarteOptionsDTO options) {
        log.info("Modification des options de la carte: {}", id);
        CarteDTO carte = carteService.modifierOptions(id, options);
        return ResponseEntity.ok(ApiResponse.success(carte, "Options modifiées avec succès"));
    }

    @PostMapping("/{id}/opposition")
    @Operation(summary = "Mettre une carte en opposition")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<CarteDTO>> mettreEnOpposition(
            @PathVariable Long id,
            @Valid @RequestBody OppositionCarteDTO request) {
        log.info("Mise en opposition de la carte: {}", id);
        CarteDTO carte = carteService.mettreEnOpposition(id, request);
        return ResponseEntity.ok(ApiResponse.success(carte, "Carte mise en opposition avec succès"));
    }

    @PostMapping("/{id}/bloquer")
    @Operation(summary = "Bloquer temporairement une carte")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<CarteDTO>> bloquerCarte(@PathVariable Long id) {
        log.info("Blocage de la carte: {}", id);
        CarteDTO carte = carteService.bloquerCarte(id);
        return ResponseEntity.ok(ApiResponse.success(carte, "Carte bloquée avec succès"));
    }

    @PostMapping("/{id}/debloquer")
    @Operation(summary = "Débloquer une carte")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    public ResponseEntity<ApiResponse<CarteDTO>> debloquerCarte(@PathVariable Long id) {
        log.info("Déblocage de la carte: {}", id);
        CarteDTO carte = carteService.debloquerCarte(id);
        return ResponseEntity.ok(ApiResponse.success(carte, "Carte débloquée avec succès"));
    }
}
