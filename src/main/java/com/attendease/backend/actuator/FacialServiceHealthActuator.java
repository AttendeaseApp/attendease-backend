package com.attendease.backend.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "facial-service")
@Slf4j
@RequiredArgsConstructor
public class FacialServiceHealthActuator {

	private final RestTemplate restTemplate;

	@Value("${extract.multiple.facial.encoding.endpoint}")
	private String facialServiceEndpoint;

	@ReadOperation
	public Map<String, Object> checkFacialService() {
		Map<String, Object> result = new HashMap<>();

		try {
			String baseUrl = facialServiceEndpoint.substring(0, facialServiceEndpoint.indexOf("/", 8));
			String healthUrl = baseUrl + "/health/status";

			var response = restTemplate.getForEntity(healthUrl, Map.class);

			result.put("status", "UP");
			result.put("endpoint", facialServiceEndpoint);
			result.put("health_check", healthUrl);
			result.put("response", response.getBody());
		} catch (Exception e) {
			result.put("status", "DOWN");
			result.put("endpoint", facialServiceEndpoint);
			result.put("error", e.getMessage());
		}

		return result;
	}
}
