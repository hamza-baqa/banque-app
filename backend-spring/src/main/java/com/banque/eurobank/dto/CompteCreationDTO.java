package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Compte;
import lombok.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteCreationDTO {
    @NotNull(message = "Le type de compte est obligatoire")
    private Compte.TypeCompte typeCompte;

    @NotBlank(message = "L'intitulé est obligatoire")
    private String intitule;

    private BigDecimal decouvertAutorise;

    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;
}
