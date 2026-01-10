package com.attendease.backend.domain.cors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 * This class binds CORS-related configuration from the application properties configs
 * (prefixed with {@code cors}) to Java objects, enabling environment-specific CORS policies.
 *
 * <p>These properties are injected into {@link com.attendease.backend.security.configurations.security.cors.CorsConfig}
 * to configure CORS policies for the OSA module web application endpoints only.</p>
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see com.attendease.backend.security.configurations.security.cors.CorsConfig
 *
 * @author jakematthewviado204@gmail.com
 * @since 2026-Jan-10
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

	private List<String> allowedOrigins;
	private String osaPathPattern;

}
