package com.banque.eurobank.repository;

import com.banque.eurobank.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Transaction
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReference(String reference);

    Page<Transaction> findByCompteId(Long compteId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.compte.id = :compteId " +
           "AND t.dateOperation BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY t.dateOperation DESC, t.id DESC")
    Page<Transaction> findByCompteAndPeriode(@Param("compteId") Long compteId,
                                              @Param("dateDebut") LocalDate dateDebut,
                                              @Param("dateFin") LocalDate dateFin,
                                              Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.compte.iban = :iban " +
           "AND (:dateDebut IS NULL OR t.dateOperation >= :dateDebut) " +
           "AND (:dateFin IS NULL OR t.dateOperation <= :dateFin) " +
           "AND (:typeOperation IS NULL OR t.typeOperation = :typeOperation) " +
           "ORDER BY t.dateOperation DESC, t.id DESC")
    Page<Transaction> rechercherTransactions(@Param("iban") String iban,
                                              @Param("dateDebut") LocalDate dateDebut,
                                              @Param("dateFin") LocalDate dateFin,
                                              @Param("typeOperation") Transaction.TypeOperation typeOperation,
                                              Pageable pageable);

    @Query("SELECT SUM(CASE WHEN t.sens = 'CREDIT' THEN t.montant ELSE 0 END) as credits, " +
           "SUM(CASE WHEN t.sens = 'DEBIT' THEN t.montant ELSE 0 END) as debits " +
           "FROM Transaction t WHERE t.compte.id = :compteId " +
           "AND t.dateOperation BETWEEN :dateDebut AND :dateFin")
    Object[] getSommeOperationsParPeriode(@Param("compteId") Long compteId,
                                          @Param("dateDebut") LocalDate dateDebut,
                                          @Param("dateFin") LocalDate dateFin);

    @Query("SELECT t FROM Transaction t WHERE t.compte.id = :compteId AND t.statut = 'EN_ATTENTE'")
    List<Transaction> findTransactionsEnAttente(@Param("compteId") Long compteId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.compte.id = :compteId " +
           "AND t.dateOperation = :date AND t.typeOperation = :type")
    Long countOperationsJournalieres(@Param("compteId") Long compteId,
                                     @Param("date") LocalDate date,
                                     @Param("type") Transaction.TypeOperation type);

    boolean existsByReference(String reference);
}
