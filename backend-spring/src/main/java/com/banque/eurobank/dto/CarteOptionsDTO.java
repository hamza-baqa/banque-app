package com.banque.eurobank.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteOptionsDTO {
    private Boolean paiementEtrangerActif;
    private Boolean retraitEtrangerActif;
    private Boolean paiementInternetActif;
    private Boolean sansContactActif;
    private BigDecimal plafondPaiementJour;
    private BigDecimal plafondRetraitJour;
}
