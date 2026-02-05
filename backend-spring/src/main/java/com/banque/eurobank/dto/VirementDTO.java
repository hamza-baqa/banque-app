package com.banque.eurobank.dto;

import lombok.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirementDTO {
    @NotNull(message = "L'IBAN émetteur est obligatoire")
    private String ibanEmetteur;

    @NotBlank(message = "L'IBAN bénéficiaire est obligatoire")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "Format IBAN invalide")
    private String ibanBeneficiaire;

    @NotBlank(message = "Le nom du bénéficiaire est obligatoire")
    @Size(max = 140)
    private String nomBeneficiaire;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant minimum est 0.01")
    @DecimalMax(value = "100000.00", message = "Le montant maximum est 100 000")
    private BigDecimal montant;

    @Size(max = 140)
    private String motif;

    private LocalDate dateExecution;

    private Boolean instantane = false;
}
