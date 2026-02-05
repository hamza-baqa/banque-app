package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Carte;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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
