package com.banque.eurobank.repository;

import com.banque.eurobank.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Compte
 */
@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {
    
    Optional<Compte> findByNumeroCompte(String numeroCompte);
    
    Optional<Compte> findByIban(String iban);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compte c WHERE c.iban = :iban")
    Optional<Compte> findByIbanForUpdate(@Param("iban") String iban);
    
    List<Compte> findByClientId(Long clientId);
    
    List<Compte> findByClientIdAndStatut(Long clientId, Compte.StatutCompte statut);
    
    @Query("SELECT c FROM Compte c WHERE c.client.id = :clientId AND c.typeCompte = :typeCompte")
    List<Compte> findByClientAndType(@Param("clientId") Long clientId, @Param("typeCompte") Compte.TypeCompte typeCompte);
    
    @Query("SELECT SUM(c.solde) FROM Compte c WHERE c.client.id = :clientId AND c.statut = 'ACTIF'")
    BigDecimal getSoldeGlobalClient(@Param("clientId") Long clientId);
    
    @Modifying
    @Query("UPDATE Compte c SET c.solde = :solde, c.soldeDisponible = :soldeDisponible, c.dateModification = :date WHERE c.id = :compteId")
    int updateSolde(@Param("compteId") Long compteId, 
                    @Param("solde") BigDecimal solde, 
                    @Param("soldeDisponible") BigDecimal soldeDisponible,
                    @Param("date") LocalDateTime date);
    
    @Query("SELECT c FROM Compte c WHERE c.agenceCode = :agenceCode AND c.statut = 'ACTIF'")
    Page<Compte> findComptesActifsParAgence(@Param("agenceCode") String agenceCode, Pageable pageable);
    
    boolean existsByIban(String iban);
    
    boolean existsByNumeroCompte(String numeroCompte);
}

/**
 * Repository Transaction
 */
@Repository
interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
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

/**
 * Repository Carte
 */
@Repository
interface CarteRepository extends JpaRepository<Carte, Long> {
    
    Optional<Carte> findByNumeroCarteHash(String numeroCarteHash);
    
    List<Carte> findByCompteId(Long compteId);
    
    @Query("SELECT c FROM Carte c WHERE c.compte.client.id = :clientId")
    List<Carte> findByClientId(@Param("clientId") Long clientId);
    
    @Query("SELECT c FROM Carte c WHERE c.compte.id = :compteId AND c.statut = 'ACTIVE'")
    List<Carte> findCartesActives(@Param("compteId") Long compteId);
    
    @Query("SELECT c FROM Carte c WHERE c.dateExpiration < :date AND c.statut = 'ACTIVE'")
    List<Carte> findCartesExpirees(@Param("date") LocalDate date);
    
    @Modifying
    @Query("UPDATE Carte c SET c.statut = 'OPPOSITION', c.opposition = true, " +
           "c.dateOpposition = :dateOpposition, c.motifOpposition = :motif WHERE c.id = :carteId")
    int mettreEnOpposition(@Param("carteId") Long carteId, 
                           @Param("dateOpposition") LocalDateTime dateOpposition,
                           @Param("motif") String motif);
    
    @Modifying
    @Query("UPDATE Carte c SET c.cumulPaiementJour = 0, c.cumulRetraitJour = 0 " +
           "WHERE c.statut = 'ACTIVE'")
    int reinitialiserCumulJournalier();
}

/**
 * Repository Utilisateur
 */
@Repository
interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByLogin(String login);
    
    Optional<Utilisateur> findByClientId(Long clientId);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.agenceCode = :agenceCode AND u.actif = true")
    List<Utilisateur> findUtilisateursActifsParAgence(@Param("agenceCode") String agenceCode);
    
    @Modifying
    @Query("UPDATE Utilisateur u SET u.tentativesConnexion = u.tentativesConnexion + 1 WHERE u.id = :userId")
    int incrementerTentatives(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Utilisateur u SET u.tentativesConnexion = 0, u.dateDerniereConnexion = :date WHERE u.id = :userId")
    int reinitialiserTentatives(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    @Modifying
    @Query("UPDATE Utilisateur u SET u.verrouille = true, u.dateVerrouillage = :date WHERE u.id = :userId")
    int verrouiller(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    boolean existsByLogin(String login);
}
