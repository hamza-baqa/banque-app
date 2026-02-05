package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Carte;
import lombok.*;
import java.time.LocalDate;

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
