package com.attendease.backend.security;

import com.attendease.backend.domain.enums.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static com.attendease.backend.security.constants.SecurityConstants.EXPIRATION_TIME;

@Component
public class JwtTokenizationUtil {

    private final String SECRET_KEY = "wisemansaidonlyfoolsrushinbuticanthelpfallinginlovewithyou";

    public String generateToken(String userId, String email, UserType userType) {
        return Jwts.builder().subject(userId)
                .claim("email", email)
                .claim("role", userType).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token).getPayload();
    }

    public Boolean validateToken(String token, String userId) {
        final String subject = extractUserId(token);
        return (subject.equals(userId) && !isTokenExpired(token));
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public boolean isTokenValid(String username, String token) {
        final String subject = getSubject(token);
        return (username != null && username.equals(subject) && !isTokenExpired(token));
    }

    public String getSubject(String token) {
        return extractUserId(token);
    }

    public List<GrantedAuthority> getAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        String role = claims.get("role", String.class);
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}
