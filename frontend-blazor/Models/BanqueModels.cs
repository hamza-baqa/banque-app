using System.ComponentModel.DataAnnotations;

namespace EuroBank.Web.Models;

// ==================== RÉPONSE API ====================

public class ApiResponse<T>
{
    public bool Success { get; set; }
    public string? Message { get; set; }
    public T? Data { get; set; }
    public string? Code { get; set; }
    public DateTime Timestamp { get; set; }
}

public class PageResponse<T>
{
    public List<T> Content { get; set; } = new();
    public int Page { get; set; }
    public int Taille { get; set; }
    public long TotalElements { get; set; }
    public int TotalPages { get; set; }
    public bool Premier { get; set; }
    public bool Dernier { get; set; }
}

// ==================== AUTHENTIFICATION ====================

public class LoginRequest
{
    [Required(ErrorMessage = "L'identifiant est obligatoire")]
    public string Login { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Le mot de passe est obligatoire")]
    public string MotDePasse { get; set; } = string.Empty;
    
    public string? CodeOtp { get; set; }
}

public class LoginResponse
{
    public string AccessToken { get; set; } = string.Empty;
    public string RefreshToken { get; set; } = string.Empty;
    public string TokenType { get; set; } = "Bearer";
    public long ExpiresIn { get; set; }
    public UtilisateurDto? Utilisateur { get; set; }
    public bool DeuxFacteursRequis { get; set; }
}

public class UtilisateurDto
{
    public long Id { get; set; }
    public string Login { get; set; } = string.Empty;
    public string TypeUtilisateur { get; set; } = string.Empty;
    public List<string> Roles { get; set; } = new();
    public string NomComplet { get; set; } = string.Empty;
    public string? AgenceCode { get; set; }
    public DateTime? DateDerniereConnexion { get; set; }
}

// ==================== CLIENT ====================

public class ClientDto
{
    public long Id { get; set; }
    public string NumeroClient { get; set; } = string.Empty;
    public string Civilite { get; set; } = string.Empty;
    public string Nom { get; set; } = string.Empty;
    public string Prenom { get; set; } = string.Empty;
    public string NomComplet { get; set; } = string.Empty;
    public DateTime? DateNaissance { get; set; }
    public string? Email { get; set; }
    public string? Telephone { get; set; }
    public string? Adresse { get; set; }
    public string? CodePostal { get; set; }
    public string? Ville { get; set; }
    public string Statut { get; set; } = string.Empty;
    public string Segment { get; set; } = string.Empty;
    public string? AgenceCode { get; set; }
    public DateTime DateCreation { get; set; }
    public List<CompteResumeDto>? Comptes { get; set; }
}

// ==================== COMPTE ====================

public class CompteDto
{
    public long Id { get; set; }
    public string NumeroCompte { get; set; } = string.Empty;
    public string Iban { get; set; } = string.Empty;
    public string Bic { get; set; } = string.Empty;
    public string Intitule { get; set; } = string.Empty;
    public string TypeCompte { get; set; } = string.Empty;
    public string Devise { get; set; } = "EUR";
    public decimal Solde { get; set; }
    public decimal SoldeDisponible { get; set; }
    public decimal DecouvertAutorise { get; set; }
    public string Statut { get; set; } = string.Empty;
    public DateTime DateOuverture { get; set; }
    public ClientResumeDto? Titulaire { get; set; }
    public List<CarteResumeDto>? Cartes { get; set; }
}

public class CompteResumeDto
{
    public long Id { get; set; }
    public string NumeroCompte { get; set; } = string.Empty;
    public string Iban { get; set; } = string.Empty;
    public string Intitule { get; set; } = string.Empty;
    public string TypeCompte { get; set; } = string.Empty;
    public decimal Solde { get; set; }
    public decimal SoldeDisponible { get; set; }
    public string Statut { get; set; } = string.Empty;
    
    public string IbanFormate => FormatIban(Iban);
    
    public string TypeCompteLibelle => TypeCompte switch
    {
        "COURANT" => "Compte Courant",
        "EPARGNE" => "Livret d'Épargne",
        "LIVRET_A" => "Livret A",
        "LDD" => "Livret Développement Durable",
        "PEL" => "Plan Épargne Logement",
        "PEA" => "Plan d'Épargne en Actions",
        "TITRE" => "Compte Titres",
        _ => TypeCompte
    };
    
    private static string FormatIban(string iban)
    {
        if (string.IsNullOrEmpty(iban)) return iban;
        return string.Join(" ", Enumerable.Range(0, (iban.Length + 3) / 4)
            .Select(i => iban.Substring(i * 4, Math.Min(4, iban.Length - i * 4))));
    }
}

public class ClientResumeDto
{
    public long Id { get; set; }
    public string NumeroClient { get; set; } = string.Empty;
    public string NomComplet { get; set; } = string.Empty;
    public string? Email { get; set; }
}

// ==================== TRANSACTION ====================

public class TransactionDto
{
    public long Id { get; set; }
    public string Reference { get; set; } = string.Empty;
    public string TypeOperation { get; set; } = string.Empty;
    public string? NatureOperation { get; set; }
    public decimal Montant { get; set; }
    public string Devise { get; set; } = "EUR";
    public string Sens { get; set; } = string.Empty;
    public string Libelle { get; set; } = string.Empty;
    public string? LibelleComplement { get; set; }
    public DateTime DateOperation { get; set; }
    public DateTime DateValeur { get; set; }
    public decimal SoldeApres { get; set; }
    public string Statut { get; set; } = string.Empty;
    public string? NomBeneficiaire { get; set; }
    public string? CompteBeneficiaire { get; set; }
    
    public bool IsCredit => Sens == "CREDIT";
    public bool IsDebit => Sens == "DEBIT";
    
    public string MontantFormate => IsCredit 
        ? $"+{Montant:N2} {Devise}" 
        : $"-{Montant:N2} {Devise}";
    
    public string TypeOperationLibelle => TypeOperation switch
    {
        "VIREMENT_EMIS" => "Virement émis",
        "VIREMENT_RECU" => "Virement reçu",
        "VIREMENT_SEPA" => "Virement SEPA",
        "VIREMENT_INSTANTANE" => "Virement instantané",
        "PRELEVEMENT" => "Prélèvement",
        "PAIEMENT_CARTE" => "Paiement carte",
        "RETRAIT_DAB" => "Retrait DAB",
        "DEPOT_ESPECES" => "Dépôt espèces",
        "DEPOT_CHEQUE" => "Dépôt chèque",
        "FRAIS_BANCAIRES" => "Frais bancaires",
        _ => TypeOperation
    };
}

// ==================== VIREMENT ====================

public class VirementRequest
{
    [Required(ErrorMessage = "Le compte émetteur est obligatoire")]
    public string IbanEmetteur { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "L'IBAN bénéficiaire est obligatoire")]
    [RegularExpression(@"^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", ErrorMessage = "Format IBAN invalide")]
    public string IbanBeneficiaire { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Le nom du bénéficiaire est obligatoire")]
    [StringLength(140, ErrorMessage = "Le nom ne peut pas dépasser 140 caractères")]
    public string NomBeneficiaire { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Le montant est obligatoire")]
    [Range(0.01, 100000, ErrorMessage = "Le montant doit être entre 0,01 € et 100 000 €")]
    public decimal Montant { get; set; }
    
    [StringLength(140, ErrorMessage = "Le motif ne peut pas dépasser 140 caractères")]
    public string? Motif { get; set; }
    
    public DateTime? DateExecution { get; set; }
    
    public bool Instantane { get; set; }
}

public class HistoriqueRequest
{
    public string Iban { get; set; } = string.Empty;
    public DateTime? DateDebut { get; set; }
    public DateTime? DateFin { get; set; }
    public string? TypeOperation { get; set; }
    public int Page { get; set; } = 0;
    public int Taille { get; set; } = 20;
}

// ==================== CARTE ====================

public class CarteDto
{
    public long Id { get; set; }
    public string NumeroCarteMasque { get; set; } = string.Empty;
    public string Titulaire { get; set; } = string.Empty;
    public string TypeCarte { get; set; } = string.Empty;
    public string Reseau { get; set; } = string.Empty;
    public DateTime DateExpiration { get; set; }
    public string Statut { get; set; } = string.Empty;
    public decimal PlafondPaiementJour { get; set; }
    public decimal PlafondRetraitJour { get; set; }
    public bool PaiementEtrangerActif { get; set; }
    public bool PaiementInternetActif { get; set; }
    public bool SansContactActif { get; set; }
    public bool Opposition { get; set; }
    
    public string TypeCarteLibelle => TypeCarte switch
    {
        "VISA_CLASSIC" => "Visa Classic",
        "VISA_PREMIER" => "Visa Premier",
        "VISA_INFINITE" => "Visa Infinite",
        "VISA_PLATINUM" => "Visa Platinum",
        "MASTERCARD_STANDARD" => "Mastercard Standard",
        "MASTERCARD_GOLD" => "Mastercard Gold",
        "MASTERCARD_WORLD_ELITE" => "Mastercard World Elite",
        _ => TypeCarte
    };
    
    public string StatutLibelle => Statut switch
    {
        "ACTIVE" => "Active",
        "BLOQUEE" => "Bloquée",
        "OPPOSITION" => "Opposition",
        "EXPIREE" => "Expirée",
        _ => Statut
    };
    
    public bool IsActive => Statut == "ACTIVE";
    public bool IsExpired => DateExpiration < DateTime.Today;
}

public class CarteResumeDto
{
    public long Id { get; set; }
    public string NumeroCarteMasque { get; set; } = string.Empty;
    public string TypeCarte { get; set; } = string.Empty;
    public string Statut { get; set; } = string.Empty;
    public DateTime DateExpiration { get; set; }
}

public class CarteOptionsRequest
{
    public bool? PaiementEtrangerActif { get; set; }
    public bool? RetraitEtrangerActif { get; set; }
    public bool? PaiementInternetActif { get; set; }
    public bool? SansContactActif { get; set; }
    public decimal? PlafondPaiementJour { get; set; }
    public decimal? PlafondRetraitJour { get; set; }
}

public class OppositionCarteRequest
{
    [Required(ErrorMessage = "Le motif est obligatoire")]
    public string Motif { get; set; } = string.Empty;
    public bool CommanderNouvelleCarte { get; set; }
}
