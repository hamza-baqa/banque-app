package com.banque.eurobank.entity;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Carte - Représente une carte bancaire
 */
@Entity
@Table(name = "EB_CARTES", indexes = {
    @Index(name = "idx_carte_numero_hash", columnList = "numero_carte_hash", unique = true),
    @Index(name = "idx_carte_compte", columnList = "compte_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_carte")
    @SequenceGenerator(name = "seq_carte", sequenceName = "EB_SEQ_CARTE", allocationSize = 1)
    private Long id;
    
    @Column(name = "numero_carte_masque", nullable = false, length = 19)
    private String numeroCarteMasque; // XXXX XXXX XXXX 1234
    
    @Column(name = "numero_carte_hash", unique = true, nullable = false, length = 64)
    private String numeroCarteHash; // SHA-256 du numéro complet
    
    @Column(name = "titulaire", nullable = false, length = 26)
    private String titulaire;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_carte", nullable = false)
    private TypeCarte typeCarte;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reseau", nullable = false)
    private ReseauCarte reseau;
    
    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;
    
    @Column(name = "date_emission")
    private LocalDate dateEmission;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    @Builder.Default
    private StatutCarte statut = StatutCarte.ACTIVE;
    
    // Plafonds
    @Column(name = "plafond_paiement_jour", precision = 19, scale = 2)
    private BigDecimal plafondPaiementJour;
    
    @Column(name = "plafond_paiement_mois", precision = 19, scale = 2)
    private BigDecimal plafondPaiementMois;
    
    @Column(name = "plafond_retrait_jour", precision = 19, scale = 2)
    private BigDecimal plafondRetraitJour;
    
    @Column(name = "plafond_retrait_semaine", precision = 19, scale = 2)
    private BigDecimal plafondRetraitSemaine;
    
    // Utilisations cumulées
    @Column(name = "cumul_paiement_jour", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cumulPaiementJour = BigDecimal.ZERO;
    
    @Column(name = "cumul_retrait_jour", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cumulRetraitJour = BigDecimal.ZERO;
    
    // Options
    @Column(name = "paiement_etranger_actif")
    @Builder.Default
    private Boolean paiementEtrangerActif = true;
    
    @Column(name = "retrait_etranger_actif")
    @Builder.Default
    private Boolean retraitEtrangerActif = true;
    
    @Column(name = "paiement_internet_actif")
    @Builder.Default
    private Boolean paiementInternetActif = true;
    
    @Column(name = "sans_contact_actif")
    @Builder.Default
    private Boolean sansContactActif = true;
    
    @Column(name = "debit_differe")
    @Builder.Default
    private Boolean debitDiffere = false;
    
    // Sécurité
    @Column(name = "code_pin_hash", length = 64)
    private String codePinHash;
    
    @Column(name = "tentatives_pin_erronees")
    @Builder.Default
    private Integer tentativesPinErronees = 0;
    
    @Column(name = "date_derniere_utilisation")
    private LocalDateTime dateDerniereUtilisation;
    
    @Column(name = "opposition")
    @Builder.Default
    private Boolean opposition = false;
    
    @Column(name = "date_opposition")
    private LocalDateTime dateOpposition;
    
    @Column(name = "motif_opposition", length = 255)
    private String motifOpposition;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (dateEmission == null) dateEmission = LocalDate.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    public enum TypeCarte {
        VISA_CLASSIC,
        VISA_PREMIER,
        VISA_INFINITE,
        VISA_PLATINUM,
        MASTERCARD_STANDARD,
        MASTERCARD_GOLD,
        MASTERCARD_WORLD_ELITE,
        CARTE_BUSINESS,
        CARTE_CORPORATE,
        CARTE_PREPAYEE
    }
    
    public enum ReseauCarte {
        VISA, MASTERCARD, CB
    }
    
    public enum StatutCarte {
        ACTIVE,
        INACTIVE,
        BLOQUEE,
        EXPIREE,
        OPPOSITION,
        ANNULEE,
        EN_FABRICATION
    }
}
