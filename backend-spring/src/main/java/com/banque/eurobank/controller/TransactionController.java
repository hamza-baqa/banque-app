package com.banque.eurobank.controller;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
public class TransactionController {

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
