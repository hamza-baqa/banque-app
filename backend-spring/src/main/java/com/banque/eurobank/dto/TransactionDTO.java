package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Transaction;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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
