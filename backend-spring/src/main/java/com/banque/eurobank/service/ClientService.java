package com.banque.eurobank.service;

import com.banque.eurobank.dto.*;
import com.banque.eurobank.entity.*;
import com.banque.eurobank.exception.*;
import com.banque.eurobank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
