package com.banque.eurobank.dto;

import lombok.*;
import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OppositionCarteDTO {
    @NotBlank(message = "Le motif est obligatoire")
    private String motif;

    private Boolean commanderNouvelleCarte = false;
}
