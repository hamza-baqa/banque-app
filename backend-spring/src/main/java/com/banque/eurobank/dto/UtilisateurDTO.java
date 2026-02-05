package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Utilisateur;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDTO {
    private Long id;
    private String login;
    private Utilisateur.TypeUtilisateur typeUtilisateur;
    private List<Utilisateur.Role> roles;
    private String nomComplet;
    private String agenceCode;
    private LocalDateTime dateDerniereConnexion;
}
