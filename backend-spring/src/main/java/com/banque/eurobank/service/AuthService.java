package com.banque.eurobank.service;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.entity.*;
import com.banque.eurobank.exception.*;
import com.banque.eurobank.repository.*;
import com.banque.eurobank.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Service d'authentification
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_TENTATIVES = 5;

    /**
     * Authentifie un utilisateur
     */
    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new AuthenticationException("Identifiants incorrects"));

        // Vérification du verrouillage
        if (utilisateur.getVerrouille()) {
            throw new CompteVerrouilleException("Compte verrouillé. Contactez votre agence.");
        }

        // Vérification du statut actif
        if (!utilisateur.getActif()) {
            throw new AuthenticationException("Compte désactivé");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getMotDePasse())
            );

            // Réinitialisation des tentatives en cas de succès
            utilisateurRepository.reinitialiserTentatives(utilisateur.getId(), LocalDateTime.now());

            // Vérification 2FA si activé
            if (utilisateur.getDeuxFacteursActif()) {
                if (request.getCodeOtp() == null || request.getCodeOtp().isEmpty()) {
                    return LoginResponseDTO.builder()
                            .deuxFacteursRequis(true)
                            .build();
                }
                // Validation OTP (à implémenter)
            }

            // Génération des tokens
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            log.info("Connexion réussie pour: {}", request.getLogin());

            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                    .utilisateur(mapToUtilisateurDTO(utilisateur))
                    .deuxFacteursRequis(false)
                    .build();

        } catch (Exception e) {
            // Incrémentation des tentatives
            utilisateurRepository.incrementerTentatives(utilisateur.getId());

            if (utilisateur.getTentativesConnexion() + 1 >= MAX_TENTATIVES) {
                utilisateurRepository.verrouiller(utilisateur.getId(), LocalDateTime.now());
                log.warn("Compte verrouillé après {} tentatives: {}", MAX_TENTATIVES, request.getLogin());
                throw new CompteVerrouilleException("Compte verrouillé après trop de tentatives");
            }

            log.warn("Échec de connexion pour: {} - Tentative {}",
                    request.getLogin(), utilisateur.getTentativesConnexion() + 1);
            throw new AuthenticationException("Identifiants incorrects");
        }
    }

    /**
     * Rafraîchit le token d'accès
     */
    public LoginResponseDTO refreshToken(String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new TokenExpireException("Token de rafraîchissement invalide ou expiré");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Utilisateur utilisateur = utilisateurRepository.findByLogin(username)
                .orElseThrow(() -> new AuthenticationException("Utilisateur non trouvé"));

        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(username);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .utilisateur(mapToUtilisateurDTO(utilisateur))
                .build();
    }

    /**
     * Déconnexion
     */
    public void logout(String username) {
        log.info("Déconnexion: {}", username);
        // Invalidation du token (à implémenter avec Redis ou base de données)
    }

    private UtilisateurDTO mapToUtilisateurDTO(Utilisateur utilisateur) {
        String nomComplet = "";
        if (utilisateur.getClient() != null) {
            nomComplet = utilisateur.getClient().getPrenom() + " " + utilisateur.getClient().getNom();
        }

        return UtilisateurDTO.builder()
                .id(utilisateur.getId())
                .login(utilisateur.getLogin())
                .typeUtilisateur(utilisateur.getTypeUtilisateur())
                .roles(utilisateur.getRoles().stream().collect(Collectors.toList()))
                .nomComplet(nomComplet)
                .agenceCode(utilisateur.getAgenceCode())
                .dateDerniereConnexion(utilisateur.getDateDerniereConnexion())
                .build();
    }
}
