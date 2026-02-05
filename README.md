#  Bank App

Application bancaire complète avec architecture enterprise moderne combinant un **frontend .NET Blazor** et un **backend Spring Boot** déployable sur **IBM WebSphere Application Server**.

##  Table des matières

- [Architecture](#architecture)
- [Technologies](#technologies)
- [Structure du projet](#structure-du-projet)
- [Installation](#installation)
  - [Démarrage rapide](#-démarrage-rapide-recommandé)
  - [Installation manuelle](#installation-manuelle-alternative)
- [Configuration](#configuration)
- [Déploiement WebSphere](#déploiement-websphere)
- [API Documentation](#api-documentation)
- [Sécurité](#sécurité)

---

##  Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              Blazor WebAssembly (.NET 7)                │    │
│  │  • Pages (Login, Dashboard, Comptes, Virements, Cartes) │    │
│  │  • Services (Auth, Compte, Virement, Carte)             │    │
│  │  • Components réutilisables                             │    │
│  └─────────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS / REST API
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        API LAYER                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              Spring Boot 2.7 (Java 11)                  │    │
│  │  • REST Controllers (/api/v1/*)                         │    │
│  │  • JWT Authentication                                   │    │
│  │  • OpenAPI / Swagger Documentation                      │    │
│  └─────────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                               │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  • ClientService     • VirementService                  │    │
│  │  • CompteService     • CarteService                     │    │
│  │  • TransactionService • AuthService                     │    │
│  └─────────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATA LAYER                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  • JPA Repositories                                     │    │
│  │  • Entities (Client, Compte, Transaction, Carte, User)  │    │
│  │  • H2 (Dev) / Oracle (WebSphere Production)             │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│               IBM WebSphere Application Server                   │
│  • Class Loader: PARENT_LAST                                    │
│  • JPA 2.1 compatibility                                        │
│  • Oracle Database / DB2                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

##  Technologies

### Backend (Spring Boot)
| Technologie | Version | Description |
|-------------|---------|-------------|
| Java | 11 | LTS compatible WebSphere |
| Spring Boot | 2.7.18 | Framework principal |
| Spring Security | 5.7.x | Authentification JWT |
| Spring Data JPA | 2.7.x | Accès données |
| H2 / Oracle | - | Base de données |
| Swagger/OpenAPI | 3.0 | Documentation API |
| Lombok | 1.18.x | Réduction boilerplate |
| MapStruct | 1.5.x | Mapping DTO |

### Frontend (Blazor)
| Technologie | Version | Description |
|-------------|---------|-------------|
| .NET | 7.0 | Framework |
| Blazor WebAssembly | 7.0 | SPA côté client |
| Bootstrap | 5.3 | CSS Framework |
| Blazored.LocalStorage | 4.4 | Stockage local |
| Blazored.Toast | 4.1 | Notifications |

---

##  Structure du projet

```
banque-app/
├── backend-spring/                    # API Spring Boot
│   ├── pom.xml                        # Configuration Maven
│   └── src/
│       └── main/
│           ├── java/com/banque/eurobank/
│           │   ├── EuroBankApplication.java
│           │   ├── config/            # Configurations
│           │   │   └── SecurityConfig.java
│           │   ├── controller/        # REST Controllers
│           │   │   └── BanqueControllers.java
│           │   ├── dto/               # Data Transfer Objects
│           │   │   └── BanqueDTO.java
│           │   ├── entity/            # Entités JPA
│           │   │   ├── Client.java
│           │   │   ├── Compte.java
│           │   │   ├── Transaction.java
│           │   │   ├── Carte.java
│           │   │   └── Utilisateur.java
│           │   ├── exception/         # Gestion des erreurs
│           │   │   ├── BanqueExceptions.java
│           │   │   └── GlobalExceptionHandler.java
│           │   ├── repository/        # Repositories JPA
│           │   │   ├── ClientRepository.java
│           │   │   └── BanqueRepositories.java
│           │   ├── security/          # Sécurité JWT
│           │   │   ├── JwtTokenProvider.java
│           │   │   ├── JwtAuthenticationFilter.java
│           │   │   └── CustomUserDetailsService.java
│           │   └── service/           # Services métier
│           │       ├── BanqueServices.java
│           │       └── ClientCarteAuthServices.java
│           ├── resources/
│           │   ├── application.yml    # Configuration
│           │   └── data.sql           # Données initiales
│           └── webapp/WEB-INF/
│               └── ibm-web-bnd.xml    # Config WebSphere
│
└── frontend-blazor/                   # Frontend .NET Blazor
    ├── EuroBank.Web.csproj            # Projet .NET
    ├── Program.cs                     # Point d'entrée
    ├── App.razor                      # Composant racine
    ├── _Imports.razor                 # Imports globaux
    ├── Models/                        # Modèles de données
    │   └── BanqueModels.cs
    ├── Services/                      # Services API
    │   └── BanqueServices.cs
    ├── Pages/                         # Pages Blazor
    │   ├── Index.razor                # Dashboard
    │   ├── Login.razor                # Connexion
    │   └── Virements.razor            # Page virements
    ├── Shared/                        # Composants partagés
    │   ├── MainLayout.razor           # Layout principal
    │   └── LoginLayout.razor          # Layout connexion
    ├── Components/                    # Composants réutilisables
    │   └── RedirectToLogin.razor
    └── wwwroot/                       # Ressources statiques
        ├── index.html
        ├── appsettings.json
        └── css/
            └── app.css
```

---

##  Installation

### Prérequis
- Java JDK 11+
- Maven 3.8+
- .NET SDK 7.0+
- Node.js 18+ (optionnel, pour les outils)

### 🚀 Démarrage rapide (Recommandé)

Le moyen le plus simple de lancer l'application complète :

```bash
# Cloner le repository
git clone https://github.com/hamza-baqa/banque-app.git
cd banque-app

# Lancer l'application complète (backend + frontend)
./start.sh
```

Le script `start.sh` va automatiquement :
- ✅ Vérifier les prérequis (Java, Maven, .NET SDK)
- ✅ Compiler et lancer le backend Spring Boot
- ✅ Compiler et lancer le frontend Blazor
- ✅ Afficher les URLs d'accès et les credentials de test
- ✅ Gérer l'arrêt propre avec Ctrl+C

**Accès à l'application :**
- **Frontend** : https://localhost:5001
- **Backend API** : http://localhost:8081/eurobank
- **Swagger UI** : http://localhost:8081/eurobank/swagger-ui.html
- **Console H2** : http://localhost:8081/eurobank/h2-console

**Arrêter l'application :**
```bash
# Appuyer sur Ctrl+C, ou
./start.sh stop
```

---

### Installation manuelle (Alternative)

Si vous préférez lancer les services individuellement :

#### Backend Spring Boot

```bash
# Naviguer vers le backend
cd backend-spring

# Compiler le projet
mvn clean package

# Lancer en mode développement
mvn spring-boot:run

# L'API sera disponible sur http://localhost:8081/eurobank
# Swagger UI: http://localhost:8081/eurobank/swagger-ui.html
# Console H2: http://localhost:8081/eurobank/h2-console
```

#### Frontend Blazor

```bash
# Naviguer vers le frontend
cd frontend-blazor

# Restaurer les packages
dotnet restore

# Lancer en mode développement
dotnet run

# L'application sera disponible sur https://localhost:5001
```

---

## ⚙️ Configuration

### Variables d'environnement (Production)

```bash
# Backend
export SPRING_PROFILES_ACTIVE=websphere
export JWT_SECRET=votre-cle-secrete-256-bits-minimum
export DB_USERNAME=eurobank_user
export DB_PASSWORD=votre_mot_de_passe

# Frontend
export ApiBaseUrl=https://api.eurobank.fr/eurobank
```

### Configuration CORS

Le backend est configuré pour accepter les requêtes depuis :
- `http://localhost:5000` (Blazor dev)
- `http://localhost:5001` (Blazor HTTPS)
- `https://www.eurobank.fr` (Production)

---

## 🖥️ Déploiement WebSphere

### Prérequis WebSphere
- WebSphere Application Server 8.5.5+
- Oracle Database ou DB2

### Configuration Class Loader

1. Dans la console WebSphere Admin, configurer le class loader en **PARENT_LAST**
2. Créer une Shared Library pour les dépendances JPA

### Build pour WebSphere

```bash
# Build avec le profil WebSphere
mvn clean package -Pwebsphere

# Le WAR sera dans target/eurobank-api.war
```

### Datasource JNDI

Configurer dans WebSphere :
- JNDI Name: `jdbc/EuroBankDS`
- Driver: Oracle JDBC ou DB2 JDBC

---

## 📖 API Documentation

### Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/auth/login` | Authentification |
| POST | `/api/v1/auth/refresh` | Rafraîchir le token |
| GET | `/api/v1/comptes/{iban}` | Détails d'un compte |
| GET | `/api/v1/comptes/client/{id}` | Comptes d'un client |
| POST | `/api/v1/virements` | Effectuer un virement |
| POST | `/api/v1/virements/instantane` | Virement instantané |
| POST | `/api/v1/transactions/historique` | Historique |
| GET | `/api/v1/cartes/{id}` | Détails carte |
| POST | `/api/v1/cartes/{id}/opposition` | Opposition carte |

### Authentification

Toutes les requêtes (sauf `/auth/login`) nécessitent un header :
```
Authorization: Bearer <jwt_token>
```

---

##  Sécurité

### Fonctionnalités de sécurité
- ✅ Authentification JWT avec refresh token
- ✅ Authentification forte (2FA) optionnelle
- ✅ Verrouillage après 5 tentatives échouées
- ✅ Hachage des mots de passe (BCrypt 12 rounds)
- ✅ Protection CSRF (API stateless)
- ✅ Validation des entrées
- ✅ Gestion des rôles (CLIENT, CONSEILLER, ADMIN)

### Comptes de test (Dev)

| Login | Mot de passe | Rôle |
|-------|--------------|------|
| jean.dupont | Demo@2024 | CLIENT |
| marie.martin | Demo@2024 | CLIENT (2FA) |
| conseiller01 | Conseiller@2024 | CONSEILLER |
| admin | Admin@2024 | ADMIN |
---

##  Équipe

Développé par l'équipe IT EuroBank
- Architecture: Département Architecture SI
- Développement: Équipe Digital Banking
- Sécurité: RSSI EuroBank
