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
 * Entité Client - Représente un client de la banque
 */
@Entity
@Table(name = "EB_CLIENTS", indexes = {
    @Index(name = "idx_client_numero", columnList = "numero_client", unique = true),
    @Index(name = "idx_client_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_client")
    @SequenceGenerator(name = "seq_client", sequenceName = "EB_SEQ_CLIENT", allocationSize = 1)
    private Long id;
    
    @Column(name = "numero_client", unique = true, nullable = false, length = 12)
    private String numeroClient;
    
    @NotBlank
    @Column(name = "civilite", length = 10)
    private String civilite;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "nom", nullable = false)
    private String nom;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "prenom", nullable = false)
    private String prenom;
    
    @Past
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    
    @Column(name = "lieu_naissance", length = 100)
    private String lieuNaissance;
    
    @Column(name = "nationalite", length = 50)
    private String nationalite;
    
    @Column(name = "piece_identite", length = 50)
    private String pieceIdentite;
    
    @Column(name = "numero_piece", length = 50)
    private String numeroPiece;
    
    @Column(name = "adresse", length = 255)
    private String adresse;
    
    @Column(name = "code_postal", length = 10)
    private String codePostal;
    
    @Column(name = "ville", length = 100)
    private String ville;
    
    @Column(name = "pays", length = 50)
    private String pays;
    
    @Email
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "telephone", length = 20)
    private String telephone;
    
    @Column(name = "telephone_mobile", length = 20)
    private String telephoneMobile;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutClient statut;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "segment")
    private SegmentClient segment;
    
    @Column(name = "conseiller_id")
    private Long conseillerId;
    
    @Column(name = "agence_code", length = 10)
    private String agenceCode;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Compte> comptes = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (statut == null) statut = StatutClient.ACTIF;
        if (segment == null) segment = SegmentClient.PARTICULIER;
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    public enum StatutClient {
        ACTIF, INACTIF, BLOQUE, EN_ATTENTE, CLOTURE
    }
    
    public enum SegmentClient {
        PARTICULIER, PROFESSIONNEL, ENTREPRISE, PREMIUM, PRIVATE_BANKING
    }
}
