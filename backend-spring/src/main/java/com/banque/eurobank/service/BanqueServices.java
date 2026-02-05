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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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

/**
 * Service pour les virements bancaires
 */
@Service
@RequiredArgsConstructor
@Slf4j
class VirementService {
    
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    
    private static final BigDecimal MONTANT_MAX_VIREMENT_INSTANTANE = new BigDecimal("15000");
    private static final int MAX_VIREMENTS_JOUR = 10;
    
    /**
     * Exécute un virement SEPA
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionDTO executerVirement(VirementDTO request, Long utilisateurId) {
        log.info("Exécution virement: {} -> {} montant: {}", 
                request.getIbanEmetteur(), request.getIbanBeneficiaire(), request.getMontant());
        
        // Validation métier
        validerVirement(request);
        
        // Récupération du compte émetteur avec verrou
        Compte compteEmetteur = compteRepository.findByIbanForUpdate(request.getIbanEmetteur())
                .orElseThrow(() -> new CompteNotFoundException("Compte émetteur non trouvé"));
        
        // Vérification du solde disponible
        BigDecimal soldeDisponible = compteEmetteur.getSoldeDisponible()
                .add(compteEmetteur.getDecouvertAutorise());
        
        if (soldeDisponible.compareTo(request.getMontant()) < 0) {
            throw new SoldeInsuffisantException("Solde insuffisant pour ce virement");
        }
        
        // Vérification du nombre de virements journaliers
        Long nbVirements = transactionRepository.countOperationsJournalieres(
                compteEmetteur.getId(), LocalDate.now(), Transaction.TypeOperation.VIREMENT_EMIS);
        if (nbVirements >= MAX_VIREMENTS_JOUR) {
            throw new LimiteDepasseeException("Nombre maximum de virements journaliers atteint");
        }
        
        // Génération de la référence unique
        String reference = genererReferenceTransaction();
        
        // Débit du compte émetteur
        BigDecimal nouveauSolde = compteEmetteur.getSolde().subtract(request.getMontant());
        BigDecimal nouveauSoldeDisponible = compteEmetteur.getSoldeDisponible().subtract(request.getMontant());
        
        compteRepository.updateSolde(compteEmetteur.getId(), nouveauSolde, nouveauSoldeDisponible, LocalDateTime.now());
        
        // Création de la transaction
        Transaction transaction = Transaction.builder()
                .reference(reference)
                .typeOperation(request.getInstantane() ? 
                        Transaction.TypeOperation.VIREMENT_INSTANTANE : 
                        Transaction.TypeOperation.VIREMENT_SEPA)
                .montant(request.getMontant())
                .devise("EUR")
                .sens(Transaction.SensOperation.DEBIT)
                .libelle("Virement vers " + request.getNomBeneficiaire())
                .libelleComplement(request.getMotif())
                .dateOperation(LocalDate.now())
                .dateValeur(request.getInstantane() ? LocalDate.now() : LocalDate.now().plusDays(1))
                .compteBeneficiaire(request.getIbanBeneficiaire())
                .nomBeneficiaire(request.getNomBeneficiaire())
                .soldeAvant(compteEmetteur.getSolde())
                .soldeApres(nouveauSolde)
                .statut(Transaction.StatutTransaction.EXECUTEE)
                .compte(compteEmetteur)
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        // Si le bénéficiaire est dans la même banque, créditer immédiatement
        compteRepository.findByIban(request.getIbanBeneficiaire()).ifPresent(compteBenef -> {
            crediterCompte(compteBenef, request.getMontant(), reference, 
                    compteEmetteur.getIban(), 
                    compteEmetteur.getClient().getPrenom() + " " + compteEmetteur.getClient().getNom(),
                    request.getMotif());
        });
        
        log.info("Virement exécuté avec succès: {}", reference);
        
        return mapToTransactionDTO(transaction);
    }
    
    /**
     * Crédite un compte (virement reçu)
     */
    private void crediterCompte(Compte compte, BigDecimal montant, String referenceOrigine,
                                String ibanEmetteur, String nomEmetteur, String motif) {
        BigDecimal nouveauSolde = compte.getSolde().add(montant);
        BigDecimal nouveauSoldeDisponible = compte.getSoldeDisponible().add(montant);
        
        compteRepository.updateSolde(compte.getId(), nouveauSolde, nouveauSoldeDisponible, LocalDateTime.now());
        
        Transaction transaction = Transaction.builder()
                .reference(genererReferenceTransaction())
                .typeOperation(Transaction.TypeOperation.VIREMENT_RECU)
                .montant(montant)
                .devise("EUR")
                .sens(Transaction.SensOperation.CREDIT)
                .libelle("Virement de " + nomEmetteur)
                .libelleComplement(motif)
                .dateOperation(LocalDate.now())
                .dateValeur(LocalDate.now())
                .compteEmetteur(ibanEmetteur)
                .nomEmetteur(nomEmetteur)
                .soldeAvant(compte.getSolde())
                .soldeApres(nouveauSolde)
                .statut(Transaction.StatutTransaction.EXECUTEE)
                .compte(compte)
                .build();
        
        transactionRepository.save(transaction);
    }
    
    /**
     * Validation des règles métier du virement
     */
    private void validerVirement(VirementDTO request) {
        if (request.getIbanEmetteur().equals(request.getIbanBeneficiaire())) {
            throw new VirementInvalideException("Le compte émetteur et bénéficiaire ne peuvent pas être identiques");
        }
        
        if (request.getInstantane() && request.getMontant().compareTo(MONTANT_MAX_VIREMENT_INSTANTANE) > 0) {
            throw new VirementInvalideException("Montant maximum pour un virement instantané: " + MONTANT_MAX_VIREMENT_INSTANTANE + " €");
        }
        
        if (request.getDateExecution() != null && request.getDateExecution().isBefore(LocalDate.now())) {
            throw new VirementInvalideException("La date d'exécution ne peut pas être dans le passé");
        }
    }
    
    /**
     * Génère une référence unique pour la transaction
     */
    private String genererReferenceTransaction() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "EB" + date + uuid;
    }
    
    private TransactionDTO mapToTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .typeOperation(transaction.getTypeOperation())
                .montant(transaction.getMontant())
                .devise(transaction.getDevise())
                .sens(transaction.getSens())
                .libelle(transaction.getLibelle())
                .dateOperation(transaction.getDateOperation())
                .dateValeur(transaction.getDateValeur())
                .soldeApres(transaction.getSoldeApres())
                .statut(transaction.getStatut())
                .nomBeneficiaire(transaction.getNomBeneficiaire())
                .compteBeneficiaire(transaction.getCompteBeneficiaire())
                .build();
    }
}

/**
 * Service pour l'historique des transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
class TransactionService {
    
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
