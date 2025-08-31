package com.attendease.backend.config;

import com.attendease.backend.attendanceTrackingService.controller.AttendanceTrackingWebSocketController;
import com.attendease.backend.eventMonitoring.handler.AttendanceWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AttendanceWebSocketHandler attendanceWebSocketHandler;
    private final AttendanceTrackingWebSocketController attendanceTrackingWebSocketController;

    public WebSocketConfig(AttendanceWebSocketHandler attendanceWebSocketHandler,
                           AttendanceTrackingWebSocketController attendanceTrackingWebSocketController) {
        this.attendanceWebSocketHandler = attendanceWebSocketHandler;
        this.attendanceTrackingWebSocketController = attendanceTrackingWebSocketController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(attendanceWebSocketHandler, "/ws/events/{eventId}/monitoring").setAllowedOrigins("*");
        registry.addHandler(attendanceTrackingWebSocketController, "/ws/checkout/{studentNumber}/ongoing/attendance/monitorLocation").setAllowedOrigins("*");
    }
}