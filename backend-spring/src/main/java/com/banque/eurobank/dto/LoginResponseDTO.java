package com.banque.eurobank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UtilisateurDTO utilisateur;
    private Boolean deuxFacteursRequis;
}
