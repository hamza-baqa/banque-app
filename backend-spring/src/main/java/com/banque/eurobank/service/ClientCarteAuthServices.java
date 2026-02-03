package com.banque.eurobank.service;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.entity.*;
import com.banque.eurobank.exception.*;
import com.banque.eurobank.repository.*;
import com.banque.eurobank.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des clients
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    /**
     * Récupère un client par ID
     */
    @Transactional(readOnly = true)
    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client non trouvé: " + id));
        return mapToClientDTO(client);
    }
    
    /**
     * Récupère un client par numéro client
     */
    @Transactional(readOnly = true)
    public ClientDTO getClientByNumero(String numeroClient) {
        Client client = clientRepository.findByNumeroClient(numeroClient)
                .orElseThrow(() -> new ClientNotFoundException("Client non trouvé: " + numeroClient));
        return mapToClientDTO(client);
    }
    
    /**
     * Crée un nouveau client
     */
    public ClientDTO creerClient(ClientCreationDTO request) {
        // Vérification de l'unicité de l'email
        if (request.getEmail() != null && clientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un client avec cet email existe déjà");
        }
        
        // Génération du numéro client
        String numeroClient = genererNumeroClient();
        
        Client client = Client.builder()
                .numeroClient(numeroClient)
                .civilite(request.getCivilite())
                .nom(request.getNom().toUpperCase())
                .prenom(capitalizeFirstLetter(request.getPrenom()))
                .dateNaissance(request.getDateNaissance())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .codePostal(request.getCodePostal())
                .ville(request.getVille())
                .pays(request.getPays() != null ? request.getPays() : "FRANCE")
                .pieceIdentite(request.getPieceIdentite())
                .numeroPiece(request.getNumeroPiece())
                .statut(Client.StatutClient.ACTIF)
                .segment(Client.SegmentClient.PARTICULIER)
                .agenceCode("00001") // Agence par défaut
                .build();
        
        client = clientRepository.save(client);
        log.info("Client créé: {} - {} {}", numeroClient, client.getPrenom(), client.getNom());
        
        return mapToClientDTO(client);
    }
    
    /**
     * Recherche de clients
     */
    @Transactional(readOnly = true)
    public PageResponse<ClientDTO> rechercherClients(String terme, int page, int taille) {
        Page<Client> pageClients = clientRepository.rechercherClients(terme, PageRequest.of(page, taille));
        
        List<ClientDTO> clients = pageClients.getContent().stream()
                .map(this::mapToClientDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<ClientDTO>builder()
                .content(clients)
                .page(pageClients.getNumber())
                .taille(pageClients.getSize())
                .totalElements(pageClients.getTotalElements())
                .totalPages(pageClients.getTotalPages())
                .premier(pageClients.isFirst())
                .dernier(pageClients.isLast())
                .build();
    }
    
    private String genererNumeroClient() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = String.format("%06d", (int) (Math.random() * 1000000));
        return "C" + timestamp + random;
    }
    
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    private ClientDTO mapToClientDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .numeroClient(client.getNumeroClient())
                .civilite(client.getCivilite())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .nomComplet(client.getPrenom() + " " + client.getNom())
                .dateNaissance(client.getDateNaissance())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .adresse(client.getAdresse())
                .codePostal(client.getCodePostal())
                .ville(client.getVille())
                .statut(client.getStatut())
                .segment(client.getSegment())
                .agenceCode(client.getAgenceCode())
                .dateCreation(client.getDateCreation())
                .build();
    }
}

/**
 * Service pour la gestion des cartes bancaires
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
class CarteService {
    
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

/**
 * Service d'authentification
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
class AuthService {
    
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
