package com.tenanthub.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterConfigTest {

    private final KeyResolver keyResolver = new RateLimiterConfig().ipKeyResolver();

    @Test
    void resolvesTheClientIpAddress() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/plans")
                .remoteAddress(new InetSocketAddress("203.0.113.42", 54321))
                .build();

        String key = keyResolver.resolve(MockServerWebExchange.from(request)).block();

        assertThat(key).isEqualTo("203.0.113.42");
    }

    @Test
    void differentClientsResolveToDifferentKeys() {
        MockServerHttpRequest requestA = MockServerHttpRequest.get("/api/plans")
                .remoteAddress(new InetSocketAddress("203.0.113.42", 54321))
                .build();
        MockServerHttpRequest requestB = MockServerHttpRequest.get("/api/plans")
                .remoteAddress(new InetSocketAddress("198.51.100.7", 11111))
                .build();

        String keyA = keyResolver.resolve(MockServerWebExchange.from(requestA)).block();
        String keyB = keyResolver.resolve(MockServerWebExchange.from(requestB)).block();

        assertThat(keyA).isNotEqualTo(keyB);
    }
}
