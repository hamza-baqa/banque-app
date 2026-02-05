package com.banque.eurobank.service;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.entity.*;
import com.banque.eurobank.exception.*;
import com.banque.eurobank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service pour les virements bancaires
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VirementService {

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
