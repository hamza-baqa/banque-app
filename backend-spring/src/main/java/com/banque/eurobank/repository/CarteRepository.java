package com.banque.eurobank.repository;

import com.banque.eurobank.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Carte
 */
@Repository
public interface CarteRepository extends JpaRepository<Carte, Long> {

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
