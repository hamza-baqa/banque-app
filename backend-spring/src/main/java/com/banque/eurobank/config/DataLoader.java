package com.banque.eurobank.config;

import com.banque.eurobank.entity.*;
import com.banque.eurobank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataLoader {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(
            ClientRepository clientRepository,
            CompteRepository compteRepository,
            TransactionRepository transactionRepository,
            CarteRepository carteRepository,
            UtilisateurRepository utilisateurRepository) {

        return args -> {
            // Check if data already exists
            if (utilisateurRepository.count() > 0) {
                log.info("Database already initialized, skipping data loading");
                return;
            }

            log.info("Initializing database with test data...");

            // Create clients
            Client client1 = Client.builder()
                    .numeroClient("C240101000001")
                    .civilite("M.")
                    .nom("DUPONT")
                    .prenom("Jean")
                    .dateNaissance(LocalDate.of(1985, 3, 15))
                    .lieuNaissance("Paris")
                    .nationalite("Française")
                    .pieceIdentite("CNI")
                    .numeroPiece("123456789012")
                    .adresse("15 Rue de la Paix")
                    .codePostal("75001")
                    .ville("Paris")
                    .pays("FRANCE")
                    .email("jean.dupont@email.com")
                    .telephone("0145678901")
                    .telephoneMobile("0612345678")
                    .statut(Client.StatutClient.ACTIF)
                    .segment(Client.SegmentClient.PARTICULIER)
                    .agenceCode("00001")
                    .build();

            Client client2 = Client.builder()
                    .numeroClient("C240101000002")
                    .civilite("Mme")
                    .nom("MARTIN")
                    .prenom("Marie")
                    .dateNaissance(LocalDate.of(1990, 7, 22))
                    .lieuNaissance("Lyon")
                    .nationalite("Française")
                    .pieceIdentite("CNI")
                    .numeroPiece("987654321098")
                    .adresse("28 Avenue des Champs-Élysées")
                    .codePostal("75008")
                    .ville("Paris")
                    .pays("FRANCE")
                    .email("marie.martin@email.com")
                    .telephone("0145678902")
                    .telephoneMobile("0698765432")
                    .statut(Client.StatutClient.ACTIF)
                    .segment(Client.SegmentClient.PREMIUM)
                    .agenceCode("00001")
                    .build();

            clientRepository.saveAll(List.of(client1, client2));
            log.info("Created 2 clients");

            // Create accounts
            Compte compte1 = Compte.builder()
                    .numeroCompte("00001234567890")
                    .iban("FR7630001000010001234567890")
                    .bic("EABORFRPP")
                    .intitule("Compte Courant Principal")
                    .typeCompte(Compte.TypeCompte.COURANT)
                    .devise("EUR")
                    .solde(new BigDecimal("5432.50"))
                    .soldeDisponible(new BigDecimal("5432.50"))
                    .decouvertAutorise(new BigDecimal("500.00"))
                    .statut(Compte.StatutCompte.ACTIF)
                    .dateOuverture(LocalDate.of(2020, 1, 15))
                    .agenceCode("00001")
                    .codeGuichet("00001")
                    .client(client1)
                    .build();

            Compte compte2 = Compte.builder()
                    .numeroCompte("00002345678901")
                    .iban("FR7630001000010002345678901")
                    .bic("EABORFRPP")
                    .intitule("Compte Courant")
                    .typeCompte(Compte.TypeCompte.COURANT)
                    .devise("EUR")
                    .solde(new BigDecimal("12750.80"))
                    .soldeDisponible(new BigDecimal("12750.80"))
                    .decouvertAutorise(new BigDecimal("2000.00"))
                    .statut(Compte.StatutCompte.ACTIF)
                    .dateOuverture(LocalDate.of(2021, 6, 1))
                    .agenceCode("00001")
                    .codeGuichet("00001")
                    .client(client2)
                    .build();

            compteRepository.saveAll(List.of(compte1, compte2));
            log.info("Created 2 accounts");

            // Create users with properly hashed passwords
            Utilisateur user1 = Utilisateur.builder()
                    .login("jean.dupont")
                    .motDePasseHash(passwordEncoder.encode("Demo@2024"))
                    .typeUtilisateur(Utilisateur.TypeUtilisateur.CLIENT)
                    .actif(true)
                    .verrouille(false)
                    .tentativesConnexion(0)
                    .premiereConnexion(false)
                    .deuxFacteursActif(false)
                    .client(client1)
                    .build();
            user1.getRoles().add(Utilisateur.Role.ROLE_CLIENT);

            Utilisateur user2 = Utilisateur.builder()
                    .login("marie.martin")
                    .motDePasseHash(passwordEncoder.encode("Demo@2024"))
                    .typeUtilisateur(Utilisateur.TypeUtilisateur.CLIENT)
                    .actif(true)
                    .verrouille(false)
                    .tentativesConnexion(0)
                    .premiereConnexion(false)
                    .deuxFacteursActif(false)
                    .client(client2)
                    .build();
            user2.getRoles().add(Utilisateur.Role.ROLE_CLIENT);

            Utilisateur conseiller = Utilisateur.builder()
                    .login("conseiller01")
                    .motDePasseHash(passwordEncoder.encode("Conseiller@2024"))
                    .typeUtilisateur(Utilisateur.TypeUtilisateur.EMPLOYE)
                    .actif(true)
                    .verrouille(false)
                    .tentativesConnexion(0)
                    .premiereConnexion(false)
                    .deuxFacteursActif(false)
                    .build();
            conseiller.getRoles().add(Utilisateur.Role.ROLE_CONSEILLER);

            Utilisateur admin = Utilisateur.builder()
                    .login("admin")
                    .motDePasseHash(passwordEncoder.encode("Admin@2024"))
                    .typeUtilisateur(Utilisateur.TypeUtilisateur.EMPLOYE)
                    .actif(true)
                    .verrouille(false)
                    .tentativesConnexion(0)
                    .premiereConnexion(false)
                    .deuxFacteursActif(false)
                    .build();
            admin.getRoles().add(Utilisateur.Role.ROLE_ADMIN_SYSTEME);
            admin.getRoles().add(Utilisateur.Role.ROLE_CONSEILLER);

            utilisateurRepository.saveAll(List.of(user1, user2, conseiller, admin));
            log.info("Created 4 users");

            log.info("Database initialization completed successfully");
            log.info("Test credentials:");
            log.info("  - jean.dupont / Demo@2024");
            log.info("  - marie.martin / Demo@2024");
            log.info("  - conseiller01 / Conseiller@2024");
            log.info("  - admin / Admin@2024");
        };
    }
}
