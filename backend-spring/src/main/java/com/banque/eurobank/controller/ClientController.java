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

/**
 * Contrôleur pour la gestion des clients
 */
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clients", description = "Gestion des clients bancaires")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un client par ID")
    @PreAuthorize("hasAnyRole('CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<ClientDTO>> getClient(
            @Parameter(description = "ID du client") @PathVariable Long id) {
        log.info("Récupération du client: {}", id);
        ClientDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(client));
    }

    @GetMapping("/numero/{numeroClient}")
    @Operation(summary = "Récupérer un client par numéro client")
    @PreAuthorize("hasAnyRole('CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<ClientDTO>> getClientByNumero(
            @Parameter(description = "Numéro client") @PathVariable String numeroClient) {
        log.info("Récupération du client par numéro: {}", numeroClient);
        ClientDTO client = clientService.getClientByNumero(numeroClient);
        return ResponseEntity.ok(ApiResponse.success(client));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau client")
    @PreAuthorize("hasAnyRole('CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<ClientDTO>> creerClient(
            @Valid @RequestBody ClientCreationDTO request) {
        log.info("Création d'un nouveau client: {} {}", request.getPrenom(), request.getNom());
        ClientDTO client = clientService.creerClient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(client, "Client créé avec succès"));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des clients")
    @PreAuthorize("hasAnyRole('CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<PageResponse<ClientDTO>>> rechercherClients(
            @RequestParam String terme,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {
        log.info("Recherche de clients: {}", terme);
        PageResponse<ClientDTO> clients = clientService.rechercherClients(terme, page, taille);
        return ResponseEntity.ok(ApiResponse.success(clients));
    }
}
