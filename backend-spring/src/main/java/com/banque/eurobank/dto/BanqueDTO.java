package com.banque.eurobank.dto;

import com.banque.eurobank.entity.*;
import lombok.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour les échanges API
 */

// ==================== CLIENT DTOs ====================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private String numeroClient;
    private String civilite;
    private String nom;
    private String prenom;
    private String nomComplet;
    private LocalDate dateNaissance;
    private String email;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String ville;
    private Client.StatutClient statut;
    private Client.SegmentClient segment;
    private String agenceCode;
    private LocalDateTime dateCreation;
    private List<CompteResumeDTO> comptes;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCreationDTO {
    @NotBlank(message = "La civilité est obligatoire")
    private String civilite;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;
    
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;
    
    @Email(message = "Format email invalide")
    private String email;
    
    private String telephone;
    private String adresse;
    private String codePostal;
    private String ville;
    private String pays;
    private String pieceIdentite;
    private String numeroPiece;
}

// ==================== COMPTE DTOs ====================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteDTO {
    private Long id;
    private String numeroCompte;
    private String iban;
    private String bic;
    private String intitule;
    private Compte.TypeCompte typeCompte;
    private String devise;
    private BigDecimal solde;
    private BigDecimal soldeDisponible;
    private BigDecimal decouvertAutorise;
    private Compte.StatutCompte statut;
    private LocalDate dateOuverture;
    private ClientResumeDTO titulaire;
    private List<CarteResumeDTO> cartes;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteResumeDTO {
    private Long id;
    private String numeroCompte;
    private String iban;
    private String intitule;
    private Compte.TypeCompte typeCompte;
    private BigDecimal solde;
    private BigDecimal soldeDisponible;
    private Compte.StatutCompte statut;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteCreationDTO {
    @NotNull(message = "Le type de compte est obligatoire")
    private Compte.TypeCompte typeCompte;
    
    @NotBlank(message = "L'intitulé est obligatoire")
    private String intitule;
    
    private BigDecimal decouvertAutorise;
    
    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;
}

// ==================== TRANSACTION DTOs ====================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;
    private String reference;
    private Transaction.TypeOperation typeOperation;
    private Transaction.NatureOperation natureOperation;
    private BigDecimal montant;
    private String devise;
    private Transaction.SensOperation sens;
    private String libelle;
    private String libelleComplement;
    private LocalDate dateOperation;
    private LocalDate dateValeur;
    private BigDecimal soldeApres;
    private Transaction.StatutTransaction statut;
    private String nomBeneficiaire;
    private String compteBeneficiaire;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirementDTO {
    @NotNull(message = "L'IBAN émetteur est obligatoire")
    private String ibanEmetteur;
    
    @NotBlank(message = "L'IBAN bénéficiaire est obligatoire")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "Format IBAN invalide")
    private String ibanBeneficiaire;
    
    @NotBlank(message = "Le nom du bénéficiaire est obligatoire")
    @Size(max = 140)
    private String nomBeneficiaire;
    
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant minimum est 0.01")
    @DecimalMax(value = "100000.00", message = "Le montant maximum est 100 000")
    private BigDecimal montant;
    
    @Size(max = 140)
    private String motif;
    
    private LocalDate dateExecution;
    
    private Boolean instantane = false;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueRequestDTO {
    @NotNull
    private String iban;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Transaction.TypeOperation typeOperation;
    private Integer page = 0;
    private Integer taille = 20;
}

// ==================== CARTE DTOs ====================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteDTO {
    private Long id;
    private String numeroCarteMasque;
    private String titulaire;
    private Carte.TypeCarte typeCarte;
    private Carte.ReseauCarte reseau;
    private LocalDate dateExpiration;
    private Carte.StatutCarte statut;
    private BigDecimal plafondPaiementJour;
    private BigDecimal plafondRetraitJour;
    private Boolean paiementEtrangerActif;
    private Boolean paiementInternetActif;
    private Boolean sansContactActif;
    private Boolean opposition;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteResumeDTO {
    private Long id;
    private String numeroCarteMasque;
    private Carte.TypeCarte typeCarte;
    private Carte.StatutCarte statut;
    private LocalDate dateExpiration;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  
public class CarteOptionsDTO {
    private Boolean paiementEtrangerActif;
    private Boolean retraitEtrangerActif;
    private Boolean paiementInternetActif;
    private Boolean sansContactActif;
    private BigDecimal plafondPaiementJour;
    private BigDecimal plafondRetraitJour;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OppositionCarteDTO {
    @NotBlank(message = "Le motif est obligatoire")
    private String motif;
    
    private Boolean commanderNouvelleCarte = false;
}

// ==================== AUTHENTIFICATION DTOs ====================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {
    @NotBlank(message = "Le login est obligatoire")
    private String login;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
    
    private String codeOtp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UtilisateurDTO utilisateur;
    private Boolean deuxFacteursRequis;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDTO {
    private Long id;
    private String login;
    private Utilisateur.TypeUtilisateur typeUtilisateur;
    private List<Utilisateur.Role> roles;
    private String nomComplet;
    private String agenceCode;
    private LocalDateTime dateDerniereConnexion;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResumeDTO {
    private Long id;
    private String numeroClient;
    private String nomComplet;
    private String email;
}

// ==================== RESPONSE WRAPPER ====================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String code;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

// ==================== PAGINATION ====================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int taille;
    private long totalElements;
    private int totalPages;
    private boolean premier;
    private boolean dernier;
}
