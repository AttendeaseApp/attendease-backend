package com.attendease.backend.configurations;

import com.attendease.backend.eventAttendanceMonitoringService.handler.AttendanceWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AttendanceWebSocketHandler attendanceWebSocketHandler;

    public WebSocketConfig(AttendanceWebSocketHandler attendanceWebSocketHandler) {
        this.attendanceWebSocketHandler = attendanceWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(attendanceWebSocketHandler, "/ws/events/{eventId}/monitoring").setAllowedOrigins("*");
    }
}