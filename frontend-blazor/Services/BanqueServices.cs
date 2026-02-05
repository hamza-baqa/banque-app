using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Security.Claims;
using System.Text.Json;
using Microsoft.AspNetCore.Components.Authorization;
using Blazored.LocalStorage;
using EuroBank.Web.Models;

namespace EuroBank.Web.Services;

// ==================== SERVICE D'AUTHENTIFICATION ====================

public interface IAuthService
{
    Task<LoginResponse?> LoginAsync(LoginRequest request);
    Task LogoutAsync();
    Task<bool> RefreshTokenAsync();
    Task<string?> GetTokenAsync();
}

public class AuthService : IAuthService
{
    private readonly HttpClient _httpClient;
    private readonly ILocalStorageService _localStorage;
    private readonly AuthenticationStateProvider _authStateProvider;
    private static readonly JsonSerializerOptions _jsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        PropertyNameCaseInsensitive = true
    };

    private const string TokenKey = "authToken";
    private const string RefreshTokenKey = "refreshToken";
    private const string UserKey = "currentUser";

    public AuthService(HttpClient httpClient,
                       ILocalStorageService localStorage,
                       AuthenticationStateProvider authStateProvider)
    {
        _httpClient = httpClient;
        _localStorage = localStorage;
        _authStateProvider = authStateProvider;
    }

    public async Task<LoginResponse?> LoginAsync(LoginRequest request)
    {
        var response = await _httpClient.PostAsJsonAsync("api/v1/auth/login", request, _jsonOptions);

        if (!response.IsSuccessStatusCode)
        {
            var errorResponse = await response.Content.ReadFromJsonAsync<ApiResponse<object>>(_jsonOptions);
            throw new Exception(errorResponse?.Message ?? "Erreur de connexion");
        }

        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<LoginResponse>>(_jsonOptions);

        if (apiResponse?.Data != null && !apiResponse.Data.DeuxFacteursRequis)
        {
            await _localStorage.SetItemAsync(TokenKey, apiResponse.Data.AccessToken);
            await _localStorage.SetItemAsync(RefreshTokenKey, apiResponse.Data.RefreshToken);
            await _localStorage.SetItemAsync(UserKey, apiResponse.Data.Utilisateur);

            ((CustomAuthStateProvider)_authStateProvider).NotifyUserAuthentication(apiResponse.Data.AccessToken);
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", apiResponse.Data.AccessToken);
        }

        return apiResponse?.Data;
    }

    public async Task LogoutAsync()
    {
        try
        {
            var token = await _localStorage.GetItemAsync<string>(TokenKey);
            if (!string.IsNullOrEmpty(token))
            {
                _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
                await _httpClient.PostAsync("api/v1/auth/logout", null);
            }
        }
        catch { }
        
        await _localStorage.RemoveItemAsync(TokenKey);
        await _localStorage.RemoveItemAsync(RefreshTokenKey);
        await _localStorage.RemoveItemAsync(UserKey);
        
        ((CustomAuthStateProvider)_authStateProvider).NotifyUserLogout();
        _httpClient.DefaultRequestHeaders.Authorization = null;
    }

    public async Task<bool> RefreshTokenAsync()
    {
        var refreshToken = await _localStorage.GetItemAsync<string>(RefreshTokenKey);
        if (string.IsNullOrEmpty(refreshToken)) return false;

        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", refreshToken);
        var response = await _httpClient.PostAsync("api/v1/auth/refresh", null);

        if (!response.IsSuccessStatusCode) return false;

        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<LoginResponse>>(_jsonOptions);
        if (apiResponse?.Data != null)
        {
            await _localStorage.SetItemAsync(TokenKey, apiResponse.Data.AccessToken);
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", apiResponse.Data.AccessToken);
            return true;
        }

        return false;
    }

    public async Task<string?> GetTokenAsync()
    {
        return await _localStorage.GetItemAsync<string>(TokenKey);
    }
}

// ==================== AUTHENTICATION STATE PROVIDER ====================

public class CustomAuthStateProvider : AuthenticationStateProvider
{
    private readonly ILocalStorageService _localStorage;
    private readonly HttpClient _httpClient;
    private readonly AuthenticationState _anonymous = new(new ClaimsPrincipal(new ClaimsIdentity()));

    public CustomAuthStateProvider(ILocalStorageService localStorage, HttpClient httpClient)
    {
        _localStorage = localStorage;
        _httpClient = httpClient;
    }

    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        var token = await _localStorage.GetItemAsync<string>("authToken");
        
        if (string.IsNullOrWhiteSpace(token))
            return _anonymous;
        
        var user = await _localStorage.GetItemAsync<UtilisateurDto>("currentUser");
        
        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        
        var claims = new List<Claim>
        {
            new(ClaimTypes.Name, user?.NomComplet ?? user?.Login ?? "Utilisateur")
        };
        
        if (user?.Roles != null)
        {
            claims.AddRange(user.Roles.Select(role => new Claim(ClaimTypes.Role, role)));
        }
        
        var identity = new ClaimsIdentity(claims, "jwt");
        var principal = new ClaimsPrincipal(identity);
        
        return new AuthenticationState(principal);
    }

    public void NotifyUserAuthentication(string token)
    {
        var identity = new ClaimsIdentity(new[] { new Claim(ClaimTypes.Name, "user") }, "jwt");
        var principal = new ClaimsPrincipal(identity);
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(principal)));
    }

    public void NotifyUserLogout()
    {
        NotifyAuthenticationStateChanged(Task.FromResult(_anonymous));
    }
}

// ==================== SERVICE DES COMPTES ====================

public interface ICompteService
{
    Task<CompteDto?> GetCompteAsync(string iban);
    Task<List<CompteResumeDto>> GetComptesClientAsync(long clientId);
    Task<decimal> GetSoldeGlobalAsync(long clientId);
}

public class CompteService : ICompteService
{
    private readonly HttpClient _httpClient;
    private readonly IAuthService _authService;

    public CompteService(HttpClient httpClient, IAuthService authService)
    {
        _httpClient = httpClient;
        _authService = authService;
    }

    private async Task SetAuthHeaderAsync()
    {
        var token = await _authService.GetTokenAsync();
        if (!string.IsNullOrEmpty(token))
        {
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        }
    }

    public async Task<CompteDto?> GetCompteAsync(string iban)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.GetFromJsonAsync<ApiResponse<CompteDto>>($"api/v1/comptes/{iban}");
        return response?.Data;
    }

    public async Task<List<CompteResumeDto>> GetComptesClientAsync(long clientId)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<CompteResumeDto>>>($"api/v1/comptes/client/{clientId}");
        return response?.Data ?? new List<CompteResumeDto>();
    }

    public async Task<decimal> GetSoldeGlobalAsync(long clientId)
    {
        var comptes = await GetComptesClientAsync(clientId);
        return comptes.Sum(c => c.Solde);
    }
}

// ==================== SERVICE DES VIREMENTS ====================

public interface IVirementService
{
    Task<TransactionDto?> EffectuerVirementAsync(VirementRequest request);
    Task<PageResponse<TransactionDto>> GetHistoriqueAsync(HistoriqueRequest request);
}

public class VirementService : IVirementService
{
    private readonly HttpClient _httpClient;
    private readonly IAuthService _authService;

    public VirementService(HttpClient httpClient, IAuthService authService)
    {
        _httpClient = httpClient;
        _authService = authService;
    }

    private async Task SetAuthHeaderAsync()
    {
        var token = await _authService.GetTokenAsync();
        if (!string.IsNullOrEmpty(token))
        {
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        }
    }

    public async Task<TransactionDto?> EffectuerVirementAsync(VirementRequest request)
    {
        await SetAuthHeaderAsync();
        
        var endpoint = request.Instantane ? "api/v1/virements/instantane" : "api/v1/virements";
        var response = await _httpClient.PostAsJsonAsync(endpoint, request);
        
        if (!response.IsSuccessStatusCode)
        {
            var errorResponse = await response.Content.ReadFromJsonAsync<ApiResponse<object>>();
            throw new Exception(errorResponse?.Message ?? "Erreur lors du virement");
        }
        
        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<TransactionDto>>();
        return apiResponse?.Data;
    }

    public async Task<PageResponse<TransactionDto>> GetHistoriqueAsync(HistoriqueRequest request)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.PostAsJsonAsync("api/v1/transactions/historique", request);
        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<PageResponse<TransactionDto>>>();
        return apiResponse?.Data ?? new PageResponse<TransactionDto>();
    }
}

// ==================== SERVICE DES CARTES ====================

public interface ICarteService
{
    Task<List<CarteDto>> GetCartesCompteAsync(long compteId);
    Task<CarteDto?> GetCarteAsync(long id);
    Task<CarteDto?> ModifierOptionsAsync(long id, CarteOptionsRequest options);
    Task<CarteDto?> BloquerCarteAsync(long id);
    Task<CarteDto?> DebloquerCarteAsync(long id);
    Task<CarteDto?> MettreEnOppositionAsync(long id, OppositionCarteRequest request);
}

public class CarteService : ICarteService
{
    private readonly HttpClient _httpClient;
    private readonly IAuthService _authService;

    public CarteService(HttpClient httpClient, IAuthService authService)
    {
        _httpClient = httpClient;
        _authService = authService;
    }

    private async Task SetAuthHeaderAsync()
    {
        var token = await _authService.GetTokenAsync();
        if (!string.IsNullOrEmpty(token))
        {
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        }
    }

    public async Task<List<CarteDto>> GetCartesCompteAsync(long compteId)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<CarteDto>>>($"api/v1/cartes/compte/{compteId}");
        return response?.Data ?? new List<CarteDto>();
    }

    public async Task<CarteDto?> GetCarteAsync(long id)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.GetFromJsonAsync<ApiResponse<CarteDto>>($"api/v1/cartes/{id}");
        return response?.Data;
    }

    public async Task<CarteDto?> ModifierOptionsAsync(long id, CarteOptionsRequest options)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.PutAsJsonAsync($"api/v1/cartes/{id}/options", options);
        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<CarteDto>>();
        return apiResponse?.Data;
    }

    public async Task<CarteDto?> BloquerCarteAsync(long id)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.PostAsync($"api/v1/cartes/{id}/bloquer", null);
        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<CarteDto>>();
        return apiResponse?.Data;
    }

    public async Task<CarteDto?> DebloquerCarteAsync(long id)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.PostAsync($"api/v1/cartes/{id}/debloquer", null);
        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<CarteDto>>();
        return apiResponse?.Data;
    }

    public async Task<CarteDto?> MettreEnOppositionAsync(long id, OppositionCarteRequest request)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.PostAsJsonAsync($"api/v1/cartes/{id}/opposition", request);
        var apiResponse = await response.Content.ReadFromJsonAsync<ApiResponse<CarteDto>>();
        return apiResponse?.Data;
    }
}

// ==================== SERVICE CLIENT ====================

public interface IClientService
{
    Task<ClientDto?> GetClientAsync(long id);
    Task<ClientDto?> GetClientByNumeroAsync(string numeroClient);
}

public class ClientService : IClientService
{
    private readonly HttpClient _httpClient;
    private readonly IAuthService _authService;

    public ClientService(HttpClient httpClient, IAuthService authService)
    {
        _httpClient = httpClient;
        _authService = authService;
    }

    private async Task SetAuthHeaderAsync()
    {
        var token = await _authService.GetTokenAsync();
        if (!string.IsNullOrEmpty(token))
        {
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        }
    }

    public async Task<ClientDto?> GetClientAsync(long id)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.GetFromJsonAsync<ApiResponse<ClientDto>>($"api/v1/clients/{id}");
        return response?.Data;
    }

    public async Task<ClientDto?> GetClientByNumeroAsync(string numeroClient)
    {
        await SetAuthHeaderAsync();
        var response = await _httpClient.GetFromJsonAsync<ApiResponse<ClientDto>>($"api/v1/clients/numero/{numeroClient}");
        return response?.Data;
    }
}
