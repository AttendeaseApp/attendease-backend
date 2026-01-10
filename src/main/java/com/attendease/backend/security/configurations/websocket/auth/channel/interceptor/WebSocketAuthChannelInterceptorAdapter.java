package com.attendease.backend.security.configurations.websocket.auth.channel.interceptor;

import com.attendease.backend.security.jwt.websocket.WebSocketAuthenticatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.attendease.backend.security.constants.SecurityConstants.JWT_TOKEN_HEADER;
import static com.attendease.backend.security.constants.SecurityConstants.TOKEN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptorAdapter implements ChannelInterceptor {

    private final WebSocketAuthenticatorService webSocketAuthenticatorService;

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.debug("No StompHeaderAccessor found in message");
            return message;
        }
        log.debug("Processing STOMP command: {}", accessor.getCommand());
        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("Processing CONNECT command");
            String jwt = extractJwtFromAccessor(accessor);
            if (jwt == null || jwt.trim().isEmpty()) {
                log.warn("Missing or empty JWT token in CONNECT frame");
                log.warn("Available headers: {}", accessor.toNativeHeaderMap());
                return createStompErrorMessage("Authentication required: Missing or empty JWT token");
            }
            log.debug("JWT token found, attempting authentication");
            try {
                final UsernamePasswordAuthenticationToken user = this.webSocketAuthenticatorService.getAuthenticatedOrFail(jwt);
                accessor.setUser(user);
                log.info("WebSocket authentication successful for user: {}", user.getName());
            } catch (AuthenticationException e) {
                log.error("WebSocket authentication failed: {}", e.getMessage(), e);
                return createStompErrorMessage("Authentication failed: " + e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error during WebSocket authentication: {}", e.getMessage(), e);
                return createStompErrorMessage("Authentication error: " + e.getMessage());
            }
        }
        return message;
    }

    private Message<?> createStompErrorMessage(String errorMessage) {
        log.warn("Creating STOMP ERROR message: {}", errorMessage);
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(errorMessage);
        errorAccessor.setLeaveMutable(true);
        errorAccessor.addNativeHeader("message", errorMessage);
        return MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
    }

    private String extractJwtFromAccessor(StompHeaderAccessor accessor) {
        String jwt = null;
        final String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        log.debug("Authorization header: {}", authHeader != null ? "present" : "missing");
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            jwt = authHeader.substring(TOKEN_PREFIX.length());
            log.debug("JWT extracted from Authorization header");
        }
        if (jwt == null || jwt.trim().isEmpty()) {
            jwt = accessor.getFirstNativeHeader(JWT_TOKEN_HEADER);
            if (jwt != null) {
                log.debug("JWT extracted from {} header", JWT_TOKEN_HEADER);
            }
        }
        if (jwt == null || jwt.trim().isEmpty()) {
            jwt = accessor.getFirstNativeHeader("token");
            if (jwt != null) {
                log.debug("JWT extracted from 'token' header");
            }
        }
        return jwt != null ? jwt.trim() : null;
    }
}