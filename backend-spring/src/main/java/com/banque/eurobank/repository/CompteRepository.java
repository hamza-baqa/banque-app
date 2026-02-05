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
