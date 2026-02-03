-- ============================================
-- EuroBank - Données d'initialisation (DEV)
-- ============================================

-- Séquences
CREATE SEQUENCE IF NOT EXISTS EB_SEQ_CLIENT START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS EB_SEQ_COMPTE START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS EB_SEQ_TRANSACTION START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS EB_SEQ_CARTE START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS EB_SEQ_UTILISATEUR START WITH 1000;

-- ============================================
-- CLIENTS
-- ============================================
INSERT INTO EB_CLIENTS (id, numero_client, civilite, nom, prenom, date_naissance, lieu_naissance, nationalite, 
                        piece_identite, numero_piece, adresse, code_postal, ville, pays, email, telephone, 
                        telephone_mobile, statut, segment, agence_code, date_creation, date_modification)
VALUES 
(1, 'C240101000001', 'M.', 'DUPONT', 'Jean', '1985-03-15', 'Paris', 'Française', 
 'CNI', '123456789012', '15 Rue de la Paix', '75001', 'Paris', 'FRANCE', 
 'jean.dupont@email.com', '0145678901', '0612345678', 'ACTIF', 'PARTICULIER', '00001', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'C240101000002', 'Mme', 'MARTIN', 'Marie', '1990-07-22', 'Lyon', 'Française',
 'CNI', '987654321098', '28 Avenue des Champs-Élysées', '75008', 'Paris', 'FRANCE',
 'marie.martin@email.com', '0145678902', '0698765432', 'ACTIF', 'PREMIUM', '00001',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'C240101000003', 'M.', 'BERNARD', 'Pierre', '1978-11-08', 'Marseille', 'Française',
 'PASSEPORT', 'FR12345678', '5 Quai de la Joliette', '13002', 'Marseille', 'FRANCE',
 'pierre.bernard@email.com', '0491234567', '0687654321', 'ACTIF', 'PRIVATE_BANKING', '00002',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================
-- COMPTES
-- ============================================
INSERT INTO EB_COMPTES (id, numero_compte, iban, bic, intitule, type_compte, devise, solde, solde_disponible,
                        decouvert_autorise, statut, date_ouverture, agence_code, code_guichet, client_id,
                        date_creation, date_modification)
VALUES
-- Comptes de Jean DUPONT
(1, '00001234567890', 'FR7630001000010001234567890', 'EABORFRPP', 'Compte Courant Principal', 
 'COURANT', 'EUR', 5432.50, 5432.50, 500.00, 'ACTIF', '2020-01-15', '00001', '00001', 1,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, '00001234567891', 'FR7630001000010001234567891', 'EABORFRPP', 'Livret A', 
 'LIVRET_A', 'EUR', 15890.00, 15890.00, 0.00, 'ACTIF', '2020-01-15', '00001', '00001', 1,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Comptes de Marie MARTIN  
(3, '00002345678901', 'FR7630001000010002345678901', 'EABORFRPP', 'Compte Courant', 
 'COURANT', 'EUR', 12750.80, 12750.80, 2000.00, 'ACTIF', '2021-06-01', '00001', '00001', 2,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(4, '00002345678902', 'FR7630001000010002345678902', 'EABORFRPP', 'PEA', 
 'PEA', 'EUR', 45000.00, 45000.00, 0.00, 'ACTIF', '2021-06-15', '00001', '00001', 2,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Comptes de Pierre BERNARD (Private Banking)
(5, '00003456789012', 'FR7630001000020003456789012', 'EABORFRPP', 'Compte Premium', 
 'COURANT', 'EUR', 125680.45, 125680.45, 50000.00, 'ACTIF', '2019-03-10', '00002', '00001', 3,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(6, '00003456789013', 'FR7630001000020003456789013', 'EABORFRPP', 'Compte Titres', 
 'TITRE', 'EUR', 350000.00, 350000.00, 0.00, 'ACTIF', '2019-03-15', '00002', '00001', 3,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================
-- TRANSACTIONS (Historique récent)
-- ============================================
INSERT INTO EB_TRANSACTIONS (id, reference, type_operation, nature_operation, montant, devise, sens, 
                             libelle, libelle_complement, date_operation, date_valeur, date_comptable,
                             solde_avant, solde_apres, statut, compte_id, date_creation)
VALUES
-- Transactions Jean DUPONT - Compte Courant
(1, 'EB20240115001A', 'VIREMENT_RECU', 'SALAIRE', 3200.00, 'EUR', 'CREDIT',
 'Virement de ENTREPRISE SA', 'Salaire Janvier 2024', '2024-01-15', '2024-01-15', '2024-01-15',
 2232.50, 5432.50, 'EXECUTEE', 1, CURRENT_TIMESTAMP),

(2, 'EB20240110002B', 'PRELEVEMENT', 'LOYER', 850.00, 'EUR', 'DEBIT',
 'PRLV IMMOBILIER PARIS', 'Loyer Janvier', '2024-01-10', '2024-01-10', '2024-01-10',
 3082.50, 2232.50, 'EXECUTEE', 1, CURRENT_TIMESTAMP),

(3, 'EB20240108003C', 'PAIEMENT_CARTE', 'ALIMENTATION', 127.85, 'EUR', 'DEBIT',
 'CARREFOUR PARIS 15', NULL, '2024-01-08', '2024-01-08', '2024-01-08',
 3210.35, 3082.50, 'EXECUTEE', 1, CURRENT_TIMESTAMP),

(4, 'EB20240105004D', 'RETRAIT_DAB', NULL, 200.00, 'EUR', 'DEBIT',
 'RETRAIT DAB PARIS OPERA', NULL, '2024-01-05', '2024-01-05', '2024-01-05',
 3410.35, 3210.35, 'EXECUTEE', 1, CURRENT_TIMESTAMP),

-- Transactions Marie MARTIN - Compte Courant
(5, 'EB20240115005E', 'VIREMENT_RECU', 'SALAIRE', 4500.00, 'EUR', 'CREDIT',
 'Virement de CONSULTING GROUP', 'Salaire + Prime', '2024-01-15', '2024-01-15', '2024-01-15',
 8250.80, 12750.80, 'EXECUTEE', 3, CURRENT_TIMESTAMP),

(6, 'EB20240112006F', 'VIREMENT_EMIS', NULL, 1500.00, 'EUR', 'DEBIT',
 'Virement vers Livret', 'Épargne mensuelle', '2024-01-12', '2024-01-12', '2024-01-12',
 9750.80, 8250.80, 'EXECUTEE', 3, CURRENT_TIMESTAMP);

-- ============================================
-- CARTES BANCAIRES
-- ============================================
INSERT INTO EB_CARTES (id, numero_carte_masque, numero_carte_hash, titulaire, type_carte, reseau,
                       date_expiration, date_emission, statut, plafond_paiement_jour, plafond_paiement_mois,
                       plafond_retrait_jour, plafond_retrait_semaine, cumul_paiement_jour, cumul_retrait_jour,
                       paiement_etranger_actif, retrait_etranger_actif, paiement_internet_actif, sans_contact_actif,
                       debit_differe, opposition, compte_id, date_creation, date_modification)
VALUES
-- Carte Jean DUPONT
(1, 'XXXX XXXX XXXX 4521', 'a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4', 'M JEAN DUPONT',
 'VISA_CLASSIC', 'VISA', '2026-12-31', '2023-01-15', 'ACTIVE', 
 1500.00, 5000.00, 300.00, 1000.00, 0.00, 0.00,
 TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Carte Marie MARTIN (Gold)
(2, 'XXXX XXXX XXXX 8734', 'b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5', 'MME MARIE MARTIN',
 'VISA_PREMIER', 'VISA', '2027-06-30', '2024-06-01', 'ACTIVE',
 3000.00, 10000.00, 500.00, 2000.00, 0.00, 0.00,
 TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Carte Pierre BERNARD (Infinite)
(3, 'XXXX XXXX XXXX 9156', 'c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6', 'M PIERRE BERNARD',
 'VISA_INFINITE', 'VISA', '2027-03-31', '2024-03-10', 'ACTIVE',
 10000.00, 50000.00, 2000.00, 5000.00, 0.00, 0.00,
 TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================
-- UTILISATEURS (Authentification)
-- ============================================
INSERT INTO EB_UTILISATEURS (id, login, mot_de_passe_hash, type_utilisateur, actif, verrouille,
                             tentatives_connexion, premiere_connexion, deux_facteurs_actif,
                             client_id, date_creation, date_modification)
VALUES
-- Client Jean DUPONT (mot de passe: Demo@2024)
(1, 'jean.dupont', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4a5T5kKLhIK0SYxe', 
 'CLIENT', TRUE, FALSE, 0, FALSE, FALSE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Client Marie MARTIN (mot de passe: Demo@2024)
(2, 'marie.martin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4a5T5kKLhIK0SYxe',
 'CLIENT', TRUE, FALSE, 0, FALSE, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Conseiller (mot de passe: Conseiller@2024)
(3, 'conseiller01', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'EMPLOYE', TRUE, FALSE, 0, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Admin (mot de passe: Admin@2024)
(4, 'admin', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'EMPLOYE', TRUE, FALSE, 0, FALSE, TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Rôles utilisateurs
INSERT INTO EB_UTILISATEUR_ROLES (utilisateur_id, role) VALUES
(1, 'ROLE_CLIENT'),
(2, 'ROLE_CLIENT'),
(3, 'ROLE_CONSEILLER'),
(4, 'ROLE_ADMIN_SYSTEME'),
(4, 'ROLE_CONSEILLER');
