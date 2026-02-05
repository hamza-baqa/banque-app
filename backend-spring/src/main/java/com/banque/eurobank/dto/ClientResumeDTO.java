package com.banque.eurobank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResumeDTO {
    private Long id;
    private String numeroClient;
    private String nomComplet;
    private String email;
}
