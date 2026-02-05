package com.banque.eurobank.controller;

import com.banque.eurobank.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur de santé et monitoring
 */
@RestController
@RequestMapping("/api/v1/health")
@Slf4j
@Tag(name = "Health", description = "Surveillance de l'état de l'application")
public class HealthController {

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
