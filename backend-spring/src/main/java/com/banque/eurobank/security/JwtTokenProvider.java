package com.banque.eurobank.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

/**
 * Gestionnaire des tokens JWT
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret:eurobank-secret-key-for-jwt-token-generation-minimum-256-bits}")
    private String jwtSecret;
    
    @Value("${app.jwt.access-token-expiration:900000}") // 15 minutes
    private long accessTokenExpiration;
    
    @Value("${app.jwt.refresh-token-expiration:604800000}") // 7 jours
    private long refreshTokenExpiration;
    
    private Key key;
    
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Génère un token d'accès
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), accessTokenExpiration);
    }
    
    /**
     * Génère un token d'accès à partir du username
     */
    public String generateAccessTokenFromUsername(String username) {
        return generateToken(username, accessTokenExpiration);
    }
    
    /**
     * Génère un token de rafraîchissement
     */
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), refreshTokenExpiration);
    }
    
    /**
     * Génère un token JWT
     */
    private String generateToken(String username, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer("EuroBank")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Extrait le username du token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * Valide un token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Token JWT malformé");
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT expiré");
        } catch (UnsupportedJwtException ex) {
            log.error("Token JWT non supporté");
        } catch (IllegalArgumentException ex) {
            log.error("Claims JWT vides");
        }
        return false;
    }
    
    public long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000; // En secondes
    }
}
