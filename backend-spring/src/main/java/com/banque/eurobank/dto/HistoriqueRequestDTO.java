package com.banque.eurobank.dto;

import com.banque.eurobank.entity.Transaction;
import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueRequestDTO {
    @NotNull
    private String iban;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Transaction.TypeOperation typeOperation;
    private Integer page = 0;
    private Integer taille = 20;
}
