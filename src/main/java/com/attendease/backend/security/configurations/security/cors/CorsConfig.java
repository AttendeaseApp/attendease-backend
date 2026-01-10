package com.attendease.backend.security.configurations.security.cors;

import com.attendease.backend.domain.cors.CorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.attendease.backend.security.constants.SecurityConstants.JWT_TOKEN_HEADER;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the RCIANS Attendease backend appl.
 *
 * <p>This configuration class sets up CORS policies specifically for the OSA (Office of Student Affairs)
 * web application endpoints.</p>
 *
 * <ul>
 *   <li><strong>OSA Module (Web App):</strong> Requires CORS configuration.</li>
 *   <li><strong>Student Module (Mobile App):</strong> Does not require CORS.</li>
 * </ul>
 *
 * @see CorsProperties
 * @see org.springframework.web.cors.CorsConfiguration
 * @see org.springframework.web.cors.CorsConfigurationSource
 *
 * @author jakematthewviado204@gmail.com
 * @since 2026-Jan-10
 */
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    /**
     * Constructs a new {@code CorsConfig} with injected CORS properties.
     * @param corsProperties The CORS configuration properties loaded from application files
     */
	public CorsConfig(CorsProperties corsProperties) {
		this.corsProperties = corsProperties;
	}

    /**
     * Configures and provides the CORS configuration source for the application.
     *
     * <p>This method creates a CORS configuration that:</p>
     * <ul>
     *   <li>Allows requests only from origins specified in {@link CorsProperties#getAllowedOrigins()}</li>
     *   <li>Permits HTTP methods: GET, POST, PUT, DELETE, OPTIONS, PATCH</li>
     *   <li>Accepts all request headers ({@code *})</li>
     *   <li>Exposes the JWT token header in responses for client-side authentication</li>
     *   <li>Allows credentials (cookies, authorization headers) to be included in requests</li>
     *   <li>Applies only to OSA module only endpoints matching the pattern in {@link CorsProperties#getOsaPathPattern()}</li>
     * </ul>
     * @return A {@link CorsConfigurationSource} that Spring Security uses to validate CORS requests
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration webConfig = new CorsConfiguration();
        webConfig.setAllowedOrigins(corsProperties.getAllowedOrigins());
        webConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        webConfig.setAllowedHeaders(List.of("*"));
        webConfig.setExposedHeaders(List.of(JWT_TOKEN_HEADER));
        webConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getOsaPathPattern(), webConfig);
        return source;
    }
}
