package com.banque.eurobank.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int taille;
    private long totalElements;
    private int totalPages;
    private boolean premier;
    private boolean dernier;
}
