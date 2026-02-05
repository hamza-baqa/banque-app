package com.banque.eurobank.config;

import com.banque.eurobank.security.JwtAuthenticationFilter;
import com.banque.eurobank.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité pour l'application EuroBank
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    // Endpoints publics (pas d'authentification requise)
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/health/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (API stateless avec JWT)
            .csrf().disable()
            
            // Configuration CORS
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            
            // Gestion des sessions stateless
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            .and()
            
            // Configuration des autorisations
            .authorizeRequests()
                // Endpoints publics
                .antMatchers(PUBLIC_ENDPOINTS).permitAll()
                
                // Endpoints clients
                .antMatchers("/api/v1/comptes/**").hasAnyRole("CLIENT", "CONSEILLER", "RESPONSABLE", "ADMIN_SYSTEME")
                .antMatchers("/api/v1/virements/**").hasAnyRole("CLIENT", "CONSEILLER", "ADMIN_SYSTEME")
                .antMatchers("/api/v1/cartes/**").hasAnyRole("CLIENT", "CONSEILLER", "ADMIN_SYSTEME")
                
                // Endpoints conseillers
                .antMatchers("/api/v1/clients/**").hasAnyRole("CONSEILLER", "RESPONSABLE", "ADMIN_SYSTEME")
                
                // Endpoints administration
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN_SYSTEME")
                
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            
            .and()
            
            // Ajouter le filtre JWT
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            
            // Gestion des exceptions
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"success\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"Authentification requise\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"success\":false,\"code\":\"ACCESS_DENIED\",\"message\":\"Accès non autorisé\"}");
                });
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5000",      // Blazor dev
                "http://localhost:5001",      // Blazor HTTPS
                "https://eurobank.local",     // Production locale
                "https://www.eurobank.fr"     // Production
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Request-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
