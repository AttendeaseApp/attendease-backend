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
            return message;
        }

        if (StompCommand.CONNECT == accessor.getCommand()) {
            String jwt = extractJwtFromAccessor(accessor);
            log.debug("Extracted JWT from CONNECT: {}", jwt != null ? "present" : "missing");
            if (jwt == null || jwt.trim().isEmpty()) {
                log.warn("Missing or empty JWT token");
                return createStompErrorMessage("Missing or empty JWT token");
            }
            try {
                final UsernamePasswordAuthenticationToken user = this.webSocketAuthenticatorService.getAuthenticatedOrFail(jwt);
                accessor.setUser(user);
                log.debug("User authenticated: {}", user.getName());
            } catch (Exception e) {
                log.warn("Auth failed for JWT: {}", e.getMessage());
                return createStompErrorMessage("Authentication failed: " + e.getMessage());
            }
        }
        return message;
    }

    private Message<?> createStompErrorMessage(String errorMessage) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(errorMessage);
        errorAccessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
    }

    private String extractJwtFromAccessor(StompHeaderAccessor accessor) {
        String jwt = null;
        final String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            jwt = authHeader.substring(TOKEN_PREFIX.length());
        }
        if (jwt == null || jwt.trim().isEmpty()) {
            jwt = accessor.getFirstNativeHeader(JWT_TOKEN_HEADER);
        }
        return jwt != null ? jwt.trim() : null;
    }
}