using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using Microsoft.AspNetCore.Components.Authorization;
using Blazored.LocalStorage;
using Blazored.Toast;
using EuroBank.Web;
using EuroBank.Web.Services;
using System.Text.Json;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");

// Configuration de l'URL de l'API
var apiBaseUrl = builder.Configuration["ApiBaseUrl"] ?? "http://localhost:8081/eurobank/";

// Configuration HttpClient pour l'API
builder.Services.AddScoped(sp => new HttpClient
{
    BaseAddress = new Uri(apiBaseUrl)
});

// Services Blazored
builder.Services.AddBlazoredLocalStorage();
builder.Services.AddBlazoredToast();

// Services d'authentification
builder.Services.AddAuthorizationCore();
builder.Services.AddScoped<AuthenticationStateProvider, CustomAuthStateProvider>();
builder.Services.AddScoped<IAuthService, AuthService>();

// Services métier
builder.Services.AddScoped<ICompteService, CompteService>();
builder.Services.AddScoped<IVirementService, VirementService>();
builder.Services.AddScoped<ICarteService, CarteService>();
builder.Services.AddScoped<IClientService, ClientService>();

await builder.Build().RunAsync();
