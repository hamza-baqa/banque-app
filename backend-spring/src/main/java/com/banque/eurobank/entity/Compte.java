package com.banque.eurobank.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Compte - Représente un compte bancaire
 */
@Entity
@Table(name = "EB_COMPTES", indexes = {
    @Index(name = "idx_compte_iban", columnList = "iban", unique = true),
    @Index(name = "idx_compte_numero", columnList = "numero_compte", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_compte")
    @SequenceGenerator(name = "seq_compte", sequenceName = "EB_SEQ_COMPTE", allocationSize = 1)
    private Long id;
    
    @Column(name = "numero_compte", unique = true, nullable = false, length = 20)
    private String numeroCompte;
    
    @Column(name = "iban", unique = true, nullable = false, length = 34)
    private String iban;
    
    @Column(name = "bic", length = 11)
    private String bic;
    
    @NotBlank
    @Column(name = "intitule", length = 100)
    private String intitule;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte", nullable = false)
    private TypeCompte typeCompte;
    
    @Column(name = "devise", length = 3)
    @Builder.Default
    private String devise = "EUR";
    
    @Column(name = "solde", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;
    
    @Column(name = "solde_disponible", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal soldeDisponible = BigDecimal.ZERO;
    
    @Column(name = "decouvert_autorise", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal decouvertAutorise = BigDecimal.ZERO;
    
    @Column(name = "taux_interet", precision = 5, scale = 4)
    private BigDecimal tauxInteret;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    @Builder.Default
    private StatutCompte statut = StatutCompte.ACTIF;
    
    @Column(name = "date_ouverture")
    private LocalDate dateOuverture;
    
    @Column(name = "date_cloture")
    private LocalDate dateCloture;
    
    @Column(name = "agence_code", length = 10)
    private String agenceCode;
    
    @Column(name = "code_guichet", length = 5)
    private String codeGuichet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dateOperation DESC")
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Carte> cartes = new ArrayList<>();
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (dateOuverture == null) dateOuverture = LocalDate.now();
        if (devise == null) devise = "EUR";
        if (bic == null) bic = "EABORFRPP"; // BIC EuroBank France
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    public enum TypeCompte {
        COURANT,           // Compte courant
        EPARGNE,           // Livret d'épargne
        LIVRET_A,          // Livret A
        LDD,               // Livret Développement Durable
        PEL,               // Plan Épargne Logement
        CEL,               // Compte Épargne Logement
        TITRE,             // Compte titres
        PEA,               // Plan d'Épargne en Actions
        TERME,             // Compte à terme
        JOINT,             // Compte joint
        PROFESSIONNEL      // Compte professionnel
    }
    
    public enum StatutCompte {
        ACTIF, INACTIF, BLOQUE, SAISI, EN_CLOTURE, CLOTURE
    }
}
