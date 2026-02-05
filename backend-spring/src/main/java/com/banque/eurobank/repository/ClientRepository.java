package com.banque.eurobank.repository;

import com.banque.eurobank.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Client
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByNumeroClient(String numeroClient);
    
    Optional<Client> findByEmail(String email);
    
    @Query("SELECT c FROM Client c WHERE c.nom LIKE %:recherche% OR c.prenom LIKE %:recherche% OR c.numeroClient LIKE %:recherche%")
    Page<Client> rechercherClients(@Param("recherche") String recherche, Pageable pageable);
    
    List<Client> findByAgenceCode(String agenceCode);
    
    List<Client> findByConseillerId(Long conseillerId);
    
    @Query("SELECT c FROM Client c WHERE c.statut = :statut")
    Page<Client> findByStatut(@Param("statut") Client.StatutClient statut, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Client c WHERE c.agenceCode = :agenceCode AND c.statut = 'ACTIF'")
    Long countClientsActifsParAgence(@Param("agenceCode") String agenceCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByNumeroClient(String numeroClient);
}
