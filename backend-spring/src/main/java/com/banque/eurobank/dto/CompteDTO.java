package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Compte;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
