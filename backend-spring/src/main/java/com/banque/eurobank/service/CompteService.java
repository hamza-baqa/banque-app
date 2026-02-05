package com.banque.eurobank.service;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.entity.*;
import com.banque.eurobank.exception.*;
import com.banque.eurobank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des comptes bancaires
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompteService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;

    private static final String CODE_BANQUE = "30001"; // Code banque EuroBank
    private static final String CODE_GUICHET_DEFAULT = "00001";
    private static final String BIC_EUROBANK = "EABORFRPP";

    /**
     * Récupère un compte par IBAN
     */
    @Transactional(readOnly = true)
    public CompteDTO getCompteByIban(String iban) {
        Compte compte = compteRepository.findByIban(iban)
                .orElseThrow(() -> new CompteNotFoundException("Compte non trouvé: " + iban));
        return mapToCompteDTO(compte);
    }

    /**
     * Récupère tous les comptes d'un client
     */
    @Transactional(readOnly = true)
    public List<CompteResumeDTO> getComptesByClient(Long clientId) {
        return compteRepository.findByClientId(clientId).stream()
                .map(this::mapToCompteResumeDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouveau compte
     */
    public CompteDTO creerCompte(CompteCreationDTO request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client non trouvé"));

        // Génération du numéro de compte et IBAN
        String numeroCompte = genererNumeroCompte();
        String iban = genererIban(numeroCompte);

        Compte compte = Compte.builder()
                .numeroCompte(numeroCompte)
                .iban(iban)
                .bic(BIC_EUROBANK)
                .intitule(request.getIntitule())
                .typeCompte(request.getTypeCompte())
                .devise("EUR")
                .solde(BigDecimal.ZERO)
                .soldeDisponible(BigDecimal.ZERO)
                .decouvertAutorise(request.getDecouvertAutorise() != null ?
                        request.getDecouvertAutorise() : BigDecimal.ZERO)
                .statut(Compte.StatutCompte.ACTIF)
                .dateOuverture(LocalDate.now())
                .agenceCode(client.getAgenceCode())
                .codeGuichet(CODE_GUICHET_DEFAULT)
                .client(client)
                .build();

        compte = compteRepository.save(compte);
        log.info("Compte créé: {} pour client: {}", iban, client.getNumeroClient());

        return mapToCompteDTO(compte);
    }

    /**
     * Récupère le solde global d'un client
     */
    @Transactional(readOnly = true)
    public BigDecimal getSoldeGlobalClient(Long clientId) {
        BigDecimal solde = compteRepository.getSoldeGlobalClient(clientId);
        return solde != null ? solde : BigDecimal.ZERO;
    }

    /**
     * Génère un numéro de compte unique
     */
    private String genererNumeroCompte() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String random = String.format("%03d", (int) (Math.random() * 1000));
        return timestamp + random;
    }

    /**
     * Génère un IBAN français
     */
    private String genererIban(String numeroCompte) {
        String bban = CODE_BANQUE + CODE_GUICHET_DEFAULT + numeroCompte + "00"; // RIB sans clé
        // Calcul simplifié de la clé RIB (algorithme complet en production)
        String cleRib = "76";
        String ibanBase = "FR" + "00" + bban + cleRib;
        // Clé de contrôle IBAN (algorithme mod 97)
        String cleIban = String.format("%02d", 98 - (new java.math.BigInteger(ibanBase.replaceAll("[A-Z]", "")
                .replace("FR", "1527")).mod(java.math.BigInteger.valueOf(97)).intValue()));
        return "FR" + cleIban + bban + cleRib;
    }

    private CompteDTO mapToCompteDTO(Compte compte) {
        return CompteDTO.builder()
                .id(compte.getId())
                .numeroCompte(compte.getNumeroCompte())
                .iban(compte.getIban())
                .bic(compte.getBic())
                .intitule(compte.getIntitule())
                .typeCompte(compte.getTypeCompte())
                .devise(compte.getDevise())
                .solde(compte.getSolde())
                .soldeDisponible(compte.getSoldeDisponible())
                .decouvertAutorise(compte.getDecouvertAutorise())
                .statut(compte.getStatut())
                .dateOuverture(compte.getDateOuverture())
                .titulaire(ClientResumeDTO.builder()
                        .id(compte.getClient().getId())
                        .numeroClient(compte.getClient().getNumeroClient())
                        .nomComplet(compte.getClient().getPrenom() + " " + compte.getClient().getNom())
                        .email(compte.getClient().getEmail())
                        .build())
                .build();
    }

    private CompteResumeDTO mapToCompteResumeDTO(Compte compte) {
        return CompteResumeDTO.builder()
                .id(compte.getId())
                .numeroCompte(compte.getNumeroCompte())
                .iban(compte.getIban())
                .intitule(compte.getIntitule())
                .typeCompte(compte.getTypeCompte())
                .solde(compte.getSolde())
                .soldeDisponible(compte.getSoldeDisponible())
                .statut(compte.getStatut())
                .build();
    }
}
