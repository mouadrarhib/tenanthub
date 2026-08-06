package com.tenanthub.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthUserClient {

    private final RestClient restClient;

    public AuthUserClient(RestClient.Builder restClientBuilder,
                           @Value("${auth-service.base-url}") String authServiceBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(authServiceBaseUrl).build();
    }

    public Optional<String> findEmail(UUID userId) {
        try {
            UserLookupResponse response = restClient.get()
                    .uri("/internal/users/{id}", userId)
                    .retrieve()
                    .body(UserLookupResponse.class);
            return Optional.ofNullable(response).map(UserLookupResponse::email);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
