package com.banque.eurobank.dto;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCreationDTO {
    @NotBlank(message = "La civilité est obligatoire")
    private String civilite;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @Email(message = "Format email invalide")
    private String email;

    private String telephone;
    private String adresse;
    private String codePostal;
    private String ville;
    private String pays;
    private String pieceIdentite;
    private String numeroPiece;
}
