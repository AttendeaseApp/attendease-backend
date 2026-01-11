package com.attendease.backend.security.jwt.websocket;

import com.attendease.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketAuthenticatorService {

    private final JwtTokenProvider jwtService;

    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final String jwt) throws AuthenticationException {
        if (jwt == null || jwt.trim().isEmpty()) {
            log.warn("WebSocket authentication failed: JWT token is null or empty");
            throw new AuthenticationCredentialsNotFoundException("JWT token is required for WebSocket authentication");
        }

        try {
            final String username = this.jwtService.extractUserId(jwt);
            if (username == null || username.trim().isEmpty()) {
                log.warn("WebSocket authentication failed: No username found in JWT token");
                throw new BadCredentialsException("Invalid JWT token: no username found");
            }
            log.debug("Extracted username from JWT: {}", username);
            if (!this.jwtService.isTokenValid(username, jwt)) {
                log.warn("WebSocket authentication failed: Invalid or expired token for user: {}", username);
                throw new BadCredentialsException("Invalid or expired JWT token");
            }
            final List<GrantedAuthority> authorities = this.jwtService.getAuthorities(jwt);
            log.info("WebSocket authentication successful for user: {} with authorities: {}", username, authorities);
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("WebSocket authentication error: {}", e.getMessage(), e);
            throw new BadCredentialsException("Failed to authenticate JWT token: " + e.getMessage(), e);
        }
    }
}