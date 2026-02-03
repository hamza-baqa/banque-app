package com.banque.eurobank.entity;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Transaction - Représente une opération bancaire
 */
@Entity
@Table(name = "EB_TRANSACTIONS", indexes = {
    @Index(name = "idx_trans_reference", columnList = "reference", unique = true),
    @Index(name = "idx_trans_compte", columnList = "compte_id"),
    @Index(name = "idx_trans_date", columnList = "date_operation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_transaction")
    @SequenceGenerator(name = "seq_transaction", sequenceName = "EB_SEQ_TRANSACTION", allocationSize = 1)
    private Long id;
    
    @Column(name = "reference", unique = true, nullable = false, length = 35)
    private String reference;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false)
    private TypeOperation typeOperation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_operation")
    private NatureOperation natureOperation;
    
    @Column(name = "montant", precision = 19, scale = 4, nullable = false)
    private BigDecimal montant;
    
    @Column(name = "devise", length = 3)
    @Builder.Default
    private String devise = "EUR";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sens")
    private SensOperation sens;
    
    @Column(name = "libelle", length = 255)
    private String libelle;
    
    @Column(name = "libelle_complement", length = 255)
    private String libelleComplement;
    
    @Column(name = "date_operation")
    private LocalDate dateOperation;
    
    @Column(name = "date_valeur")
    private LocalDate dateValeur;
    
    @Column(name = "date_comptable")
    private LocalDate dateComptable;
    
    // Informations virement
    @Column(name = "compte_emetteur", length = 34)
    private String compteEmetteur;
    
    @Column(name = "compte_beneficiaire", length = 34)
    private String compteBeneficiaire;
    
    @Column(name = "nom_emetteur", length = 140)
    private String nomEmetteur;
    
    @Column(name = "nom_beneficiaire", length = 140)
    private String nomBeneficiaire;
    
    @Column(name = "bic_emetteur", length = 11)
    private String bicEmetteur;
    
    @Column(name = "bic_beneficiaire", length = 11)
    private String bicBeneficiaire;
    
    // Informations carte
    @Column(name = "numero_carte_masque", length = 19)
    private String numeroCarteMasque;
    
    @Column(name = "nom_commercant", length = 100)
    private String nomCommercant;
    
    @Column(name = "mcc_code", length = 4)
    private String mccCode;
    
    @Column(name = "pays_operation", length = 3)
    private String paysOperation;
    
    // Soldes après opération
    @Column(name = "solde_avant", precision = 19, scale = 4)
    private BigDecimal soldeAvant;
    
    @Column(name = "solde_apres", precision = 19, scale = 4)
    private BigDecimal soldeApres;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    @Builder.Default
    private StatutTransaction statut = StatutTransaction.EXECUTEE;
    
    @Column(name = "motif_rejet", length = 255)
    private String motifRejet;
    
    @Column(name = "code_retour", length = 10)
    private String codeRetour;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        if (dateOperation == null) dateOperation = LocalDate.now();
        if (dateValeur == null) dateValeur = dateOperation;
        if (devise == null) devise = "EUR";
    }
    
    public enum TypeOperation {
        VIREMENT_EMIS,
        VIREMENT_RECU,
        VIREMENT_SEPA,
        VIREMENT_INSTANTANE,
        PRELEVEMENT,
        PAIEMENT_CARTE,
        RETRAIT_DAB,
        DEPOT_ESPECES,
        DEPOT_CHEQUE,
        REMISE_CHEQUE,
        CHEQUE_EMIS,
        FRAIS_BANCAIRES,
        INTERETS,
        COMMISSION,
        REGULARISATION,
        AVOIR
    }
    
    public enum NatureOperation {
        SALAIRE,
        LOYER,
        FACTURE,
        ALIMENTATION,
        TRANSPORT,
        LOISIRS,
        SANTE,
        ASSURANCE,
        IMPOTS,
        EPARGNE,
        DIVERS
    }
    
    public enum SensOperation {
        CREDIT, DEBIT
    }
    
    public enum StatutTransaction {
        EN_ATTENTE,
        EN_COURS,
        EXECUTEE,
        REJETEE,
        ANNULEE,
        SUSPENDUE
    }
}
