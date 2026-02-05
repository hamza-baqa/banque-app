package com.banque.eurobank.service;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.entity.*;
import com.banque.eurobank.exception.*;
import com.banque.eurobank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour l'historique des transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CompteRepository compteRepository;

    /**
     * Récupère l'historique des transactions
     */
    @Transactional(readOnly = true)
    public PageResponse<TransactionDTO> getHistorique(HistoriqueRequestDTO request) {
        Compte compte = compteRepository.findByIban(request.getIban())
                .orElseThrow(() -> new CompteNotFoundException("Compte non trouvé"));

        LocalDate dateDebut = request.getDateDebut() != null ?
                request.getDateDebut() : LocalDate.now().minusMonths(3);
        LocalDate dateFin = request.getDateFin() != null ?
                request.getDateFin() : LocalDate.now();

        Pageable pageable = PageRequest.of(request.getPage(), request.getTaille());

        Page<Transaction> page = transactionRepository.rechercherTransactions(
                request.getIban(), dateDebut, dateFin, request.getTypeOperation(), pageable);

        List<TransactionDTO> transactions = page.getContent().stream()
                .map(this::mapToTransactionDTO)
                .collect(Collectors.toList());

        return PageResponse.<TransactionDTO>builder()
                .content(transactions)
                .page(page.getNumber())
                .taille(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .premier(page.isFirst())
                .dernier(page.isLast())
                .build();
    }

    private TransactionDTO mapToTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .typeOperation(transaction.getTypeOperation())
                .natureOperation(transaction.getNatureOperation())
                .montant(transaction.getMontant())
                .devise(transaction.getDevise())
                .sens(transaction.getSens())
                .libelle(transaction.getLibelle())
                .libelleComplement(transaction.getLibelleComplement())
                .dateOperation(transaction.getDateOperation())
                .dateValeur(transaction.getDateValeur())
                .soldeApres(transaction.getSoldeApres())
                .statut(transaction.getStatut())
                .nomBeneficiaire(transaction.getNomBeneficiaire())
                .compteBeneficiaire(transaction.getCompteBeneficiaire())
                .build();
    }
}
