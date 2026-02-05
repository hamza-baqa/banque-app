package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Client;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private String numeroClient;
    private String civilite;
    private String nom;
    private String prenom;
    private String nomComplet;
    private LocalDate dateNaissance;
    private String email;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String ville;
    private Client.StatutClient statut;
    private Client.SegmentClient segment;
    private String agenceCode;
    private LocalDateTime dateCreation;
    private List<CompteResumeDTO> comptes;
}
