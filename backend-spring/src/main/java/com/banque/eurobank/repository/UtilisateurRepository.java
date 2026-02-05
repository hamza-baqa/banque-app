package com.banque.eurobank.repository;

import com.banque.eurobank.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Utilisateur
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

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
