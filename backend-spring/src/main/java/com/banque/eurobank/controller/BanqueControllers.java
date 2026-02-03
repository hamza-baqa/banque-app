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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
class VirementController {
    
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

/**
 * Contrôleur pour l'historique des transactions
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Historique et détails des transactions")
@SecurityRequirement(name = "bearerAuth")
@Validated
class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping("/historique")
    @Operation(summary = "Récupérer l'historique des transactions")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE')")
    public ResponseEntity<ApiResponse<PageResponse<TransactionDTO>>> getHistorique(
            @Valid @RequestBody HistoriqueRequestDTO request) {
        log.info("Récupération historique pour compte: {}", request.getIban());
        PageResponse<TransactionDTO> historique = transactionService.getHistorique(request);
        return ResponseEntity.ok(ApiResponse.success(historique));
    }
}

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
class ClientController {
    
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
class CarteController {
    
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

/**
 * Contrôleur d'authentification
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Gestion de l'authentification")
@Validated
class AuthController {
    
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

/**
 * Contrôleur de santé et monitoring
 */
@RestController
@RequestMapping("/api/v1/health")
@Slf4j
@Tag(name = "Health", description = "Surveillance de l'état de l'application")
class HealthController {
    
    @GetMapping
    @Operation(summary = "Vérifier l'état de l'application")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("UP", "Application EuroBank opérationnelle"));
    }
    
    @GetMapping("/ready")
    @Operation(summary = "Vérifier si l'application est prête")
    public ResponseEntity<ApiResponse<String>> ready() {
        return ResponseEntity.ok(ApiResponse.success("READY"));
    }
}
