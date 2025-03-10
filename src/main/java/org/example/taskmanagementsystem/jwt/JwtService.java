package org.example.taskmanagementsystem.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private final long ACCESS_TOKEN_VALIDITY = 1_800_000; // 30 минут
    private final long REFRESH_TOKEN_VALIDITY = 604_800_000; // 7 дней


    private String createToken(Map<String, Object> claims, String userName, long validity, String type) {
        claims.put("typ", type);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, String> generateTokens(String userName, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        String accessToken = createToken(claims, userName, ACCESS_TOKEN_VALIDITY, "access");
        String refreshToken = createToken(claims, userName, REFRESH_TOKEN_VALIDITY, "refresh");
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        String tokenType = extractClaim(token, claims -> claims.get("typ", String.class));

        return username.equals(userDetails.getUsername()) && !isTokenExpired(token)
                && "access".equals(tokenType);
    }


    public Boolean validateRefreshToken(String token) {
        return !isTokenExpired(token);
    }
    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
