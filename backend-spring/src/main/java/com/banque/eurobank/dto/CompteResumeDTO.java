package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Compte;
import lombok.*;
import java.math.BigDecimal;

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
