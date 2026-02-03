package com.banque.eurobank.exception;

import com.banque.eurobank.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions pour l'API EuroBank
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // ==================== EXCEPTIONS MÉTIER ====================
    
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleClientNotFound(ClientNotFoundException ex) {
        log.warn("Client non trouvé: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "CLIENT_NOT_FOUND"));
    }
    
    @ExceptionHandler(CompteNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCompteNotFound(CompteNotFoundException ex) {
        log.warn("Compte non trouvé: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "COMPTE_NOT_FOUND"));
    }
    
    @ExceptionHandler(SoldeInsuffisantException.class)
    public ResponseEntity<ApiResponse<Void>> handleSoldeInsuffisant(SoldeInsuffisantException ex) {
        log.warn("Solde insuffisant: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "SOLDE_INSUFFISANT"));
    }
    
    @ExceptionHandler(VirementInvalideException.class)
    public ResponseEntity<ApiResponse<Void>> handleVirementInvalide(VirementInvalideException ex) {
        log.warn("Virement invalide: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "VIREMENT_INVALIDE"));
    }
    
    @ExceptionHandler(LimiteDepasseeException.class)
    public ResponseEntity<ApiResponse<Void>> handleLimiteDepassee(LimiteDepasseeException ex) {
        log.warn("Limite dépassée: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "LIMITE_DEPASSEE"));
    }
    
    @ExceptionHandler(CompteBloqueException.class)
    public ResponseEntity<ApiResponse<Void>> handleCompteBloque(CompteBloqueException ex) {
        log.warn("Compte bloqué: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "COMPTE_BLOQUE"));
    }
    
    @ExceptionHandler(CarteNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCarteNotFound(CarteNotFoundException ex) {
        log.warn("Carte non trouvée: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "CARTE_NOT_FOUND"));
    }
    
    @ExceptionHandler(OperationNonAutoriseeException.class)
    public ResponseEntity<ApiResponse<Void>> handleOperationNonAutorisee(OperationNonAutoriseeException ex) {
        log.warn("Opération non autorisée: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "OPERATION_NON_AUTORISEE"));
    }
    
    // ==================== EXCEPTIONS AUTHENTIFICATION ====================
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("Erreur d'authentification: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), "AUTH_ERROR"));
    }
    
    @ExceptionHandler(CompteVerrouilleException.class)
    public ResponseEntity<ApiResponse<Void>> handleCompteVerrouille(CompteVerrouilleException ex) {
        log.warn("Compte utilisateur verrouillé: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "COMPTE_VERROUILLE"));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Accès refusé: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Accès non autorisé à cette ressource", "ACCESS_DENIED"));
    }
    
    @ExceptionHandler(TokenExpireException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpire(TokenExpireException ex) {
        log.warn("Token expiré: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), "TOKEN_EXPIRED"));
    }
    
    // ==================== EXCEPTIONS VALIDATION ====================
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Erreurs de validation: {}", errors);
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Erreurs de validation")
                .code("VALIDATION_ERROR")
                .data(errors)
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (v1, v2) -> v1
                ));
        
        log.warn("Violations de contraintes: {}", errors);
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Erreurs de validation")
                .code("CONSTRAINT_VIOLATION")
                .data(errors)
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // ==================== EXCEPTIONS TECHNIQUES ====================
    
    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ApiResponse<Void>> handleTechnical(TechnicalException ex) {
        log.error("Erreur technique: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Une erreur technique est survenue", "TECHNICAL_ERROR"));
    }
    
    @ExceptionHandler(ServiceIndisponibleException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceIndisponible(ServiceIndisponibleException ex) {
        log.error("Service indisponible: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(ex.getMessage(), "SERVICE_UNAVAILABLE"));
    }
    
    // ==================== EXCEPTION GÉNÉRIQUE ====================
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Erreur non gérée: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Une erreur inattendue est survenue. Veuillez réessayer ultérieurement.",
                        "INTERNAL_ERROR"
                ));
    }
}
