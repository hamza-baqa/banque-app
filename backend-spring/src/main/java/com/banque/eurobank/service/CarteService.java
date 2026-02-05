package com.banque.eurobank.service;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.entity.*;
import com.banque.eurobank.exception.*;
import com.banque.eurobank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des cartes bancaires
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CarteService {

    private final CarteRepository carteRepository;

    /**
     * Récupère les cartes d'un compte
     */
    @Transactional(readOnly = true)
    public List<CarteDTO> getCartesByCompte(Long compteId) {
        return carteRepository.findByCompteId(compteId).stream()
                .map(this::mapToCarteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une carte par ID
     */
    @Transactional(readOnly = true)
    public CarteDTO getCarteById(Long id) {
        Carte carte = carteRepository.findById(id)
                .orElseThrow(() -> new CarteNotFoundException("Carte non trouvée: " + id));
        return mapToCarteDTO(carte);
    }

    /**
     * Modifie les options d'une carte
     */
    public CarteDTO modifierOptions(Long id, CarteOptionsDTO options) {
        Carte carte = carteRepository.findById(id)
                .orElseThrow(() -> new CarteNotFoundException("Carte non trouvée: " + id));

        if (carte.getStatut() != Carte.StatutCarte.ACTIVE) {
            throw new CarteBloqueException("Impossible de modifier les options d'une carte non active");
        }

        if (options.getPaiementEtrangerActif() != null) {
            carte.setPaiementEtrangerActif(options.getPaiementEtrangerActif());
        }
        if (options.getRetraitEtrangerActif() != null) {
            carte.setRetraitEtrangerActif(options.getRetraitEtrangerActif());
        }
        if (options.getPaiementInternetActif() != null) {
            carte.setPaiementInternetActif(options.getPaiementInternetActif());
        }
        if (options.getSansContactActif() != null) {
            carte.setSansContactActif(options.getSansContactActif());
        }
        if (options.getPlafondPaiementJour() != null) {
            carte.setPlafondPaiementJour(options.getPlafondPaiementJour());
        }
        if (options.getPlafondRetraitJour() != null) {
            carte.setPlafondRetraitJour(options.getPlafondRetraitJour());
        }

        carte = carteRepository.save(carte);
        log.info("Options de la carte {} modifiées", carte.getNumeroCarteMasque());

        return mapToCarteDTO(carte);
    }

    /**
     * Met une carte en opposition
     */
    public CarteDTO mettreEnOpposition(Long id, OppositionCarteDTO request) {
        Carte carte = carteRepository.findById(id)
                .orElseThrow(() -> new CarteNotFoundException("Carte non trouvée: " + id));

        if (carte.getOpposition()) {
            throw new IllegalStateException("Cette carte est déjà en opposition");
        }

        carteRepository.mettreEnOpposition(id, LocalDateTime.now(), request.getMotif());

        log.warn("Carte {} mise en opposition - Motif: {}", carte.getNumeroCarteMasque(), request.getMotif());

        carte.setStatut(Carte.StatutCarte.OPPOSITION);
        carte.setOpposition(true);
        carte.setDateOpposition(LocalDateTime.now());
        carte.setMotifOpposition(request.getMotif());

        return mapToCarteDTO(carte);
    }

    /**
     * Bloque temporairement une carte
     */
    public CarteDTO bloquerCarte(Long id) {
        Carte carte = carteRepository.findById(id)
                .orElseThrow(() -> new CarteNotFoundException("Carte non trouvée: " + id));

        carte.setStatut(Carte.StatutCarte.BLOQUEE);
        carte = carteRepository.save(carte);

        log.info("Carte {} bloquée temporairement", carte.getNumeroCarteMasque());

        return mapToCarteDTO(carte);
    }

    /**
     * Débloque une carte
     */
    public CarteDTO debloquerCarte(Long id) {
        Carte carte = carteRepository.findById(id)
                .orElseThrow(() -> new CarteNotFoundException("Carte non trouvée: " + id));

        if (carte.getStatut() == Carte.StatutCarte.OPPOSITION) {
            throw new IllegalStateException("Impossible de débloquer une carte en opposition");
        }

        carte.setStatut(Carte.StatutCarte.ACTIVE);
        carte = carteRepository.save(carte);

        log.info("Carte {} débloquée", carte.getNumeroCarteMasque());

        return mapToCarteDTO(carte);
    }

    private CarteDTO mapToCarteDTO(Carte carte) {
        return CarteDTO.builder()
                .id(carte.getId())
                .numeroCarteMasque(carte.getNumeroCarteMasque())
                .titulaire(carte.getTitulaire())
                .typeCarte(carte.getTypeCarte())
                .reseau(carte.getReseau())
                .dateExpiration(carte.getDateExpiration())
                .statut(carte.getStatut())
                .plafondPaiementJour(carte.getPlafondPaiementJour())
                .plafondRetraitJour(carte.getPlafondRetraitJour())
                .paiementEtrangerActif(carte.getPaiementEtrangerActif())
                .paiementInternetActif(carte.getPaiementInternetActif())
                .sansContactActif(carte.getSansContactActif())
                .opposition(carte.getOpposition())
                .build();
    }
}
