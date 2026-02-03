package com.banque.eurobank.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité Utilisateur - Gestion des accès au système bancaire
 */
@Entity
@Table(name = "EB_UTILISATEURS", indexes = {
    @Index(name = "idx_user_login", columnList = "login", unique = true),
    @Index(name = "idx_user_client", columnList = "client_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_utilisateur")
    @SequenceGenerator(name = "seq_utilisateur", sequenceName = "EB_SEQ_UTILISATEUR", allocationSize = 1)
    private Long id;
    
    @Column(name = "login", unique = true, nullable = false, length = 50)
    private String login;
    
    @Column(name = "mot_de_passe_hash", nullable = false, length = 255)
    private String motDePasseHash;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "EB_UTILISATEUR_ROLES", joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_utilisateur")
    private TypeUtilisateur typeUtilisateur;
    
    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;
    
    @Column(name = "verrouille")
    @Builder.Default
    private Boolean verrouille = false;
    
    @Column(name = "tentatives_connexion")
    @Builder.Default
    private Integer tentativesConnexion = 0;
    
    @Column(name = "date_derniere_connexion")
    private LocalDateTime dateDerniereConnexion;
    
    @Column(name = "date_verrouillage")
    private LocalDateTime dateVerrouillage;
    
    @Column(name = "date_expiration_mdp")
    private LocalDateTime dateExpirationMdp;
    
    @Column(name = "premiere_connexion")
    @Builder.Default
    private Boolean premiereConnexion = true;
    
    // Authentification forte
    @Column(name = "otp_secret", length = 64)
    private String otpSecret;
    
    @Column(name = "deux_facteurs_actif")
    @Builder.Default
    private Boolean deuxFacteursActif = false;
    
    @Column(name = "telephone_validation", length = 20)
    private String telephoneValidation;
    
    // Lien avec le client (pour les clients en ligne)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
    
    // Pour les employés
    @Column(name = "matricule", length = 20)
    private String matricule;
    
    @Column(name = "agence_code", length = 10)
    private String agenceCode;
    
    @Column(name = "service", length = 50)
    private String service;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    public enum Role {
        ROLE_CLIENT,           // Client banque en ligne
        ROLE_CONSEILLER,       // Conseiller clientèle
        ROLE_RESPONSABLE,      // Responsable d'agence
        ROLE_BACK_OFFICE,      // Back office
        ROLE_RISK_MANAGER,     // Gestion des risques
        ROLE_COMPLIANCE,       // Conformité
        ROLE_AUDIT,            // Audit interne
        ROLE_ADMIN_SYSTEME,    // Administrateur système
        ROLE_SUPER_ADMIN       // Super administrateur
    }
    
    public enum TypeUtilisateur {
        CLIENT,        // Client de la banque
        EMPLOYE,       // Employé de la banque
        PARTENAIRE,    // Partenaire externe
        TECHNIQUE      // Compte technique
    }
}
