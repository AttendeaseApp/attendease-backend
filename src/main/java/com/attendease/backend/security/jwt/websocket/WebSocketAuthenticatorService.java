package com.attendease.backend.security.jwt.websocket;

import com.attendease.backend.security.JwtTokenizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebSocketAuthenticatorService {

    private final JwtTokenizationUtil jwtService;

    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final String  jwt) throws AuthenticationException {
        if (jwt == null || jwt.trim().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Username was null or empty.");
        }
        final String username = this.jwtService.extractUserId(jwt);
        final List<GrantedAuthority> authorities = this.jwtService.getAuthorities(jwt);
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}

