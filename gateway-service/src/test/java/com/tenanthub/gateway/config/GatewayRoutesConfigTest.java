package com.tenanthub.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// eureka.client.enabled=false keeps this fast and independent of discovery-service
// being up - route binding is what's under test, not registration.
//
// This is the test that would have caught the real bug hit building P5: routes were
// written under spring.cloud.gateway.routes (the classic prefix most Gateway docs and
// tutorials use), but this Spring Cloud version's reactive module binds config under
// spring.cloud.gateway.server.webflux.routes instead - the old prefix silently bound
// to an empty list, no error, every request just 404'd as if no route existed.
@SpringBootTest(properties = "eureka.client.enabled=false")
class GatewayRoutesConfigTest {

    @Autowired
    private GatewayProperties gatewayProperties;

    @Test
    void allFourServiceRoutesAreLoaded() {
        List<RouteDefinition> routes = gatewayProperties.getRoutes();

        assertThat(routes)
                .extracting(RouteDefinition::getId)
                .containsExactlyInAnyOrder("auth-route", "tenant-route", "project-route", "billing-route");
    }

    @Test
    void everyRouteTargetsItsServiceByEurekaNameNotAHardcodedAddress() {
        Map<String, String> routeIdToUri = gatewayProperties.getRoutes().stream()
                .collect(java.util.stream.Collectors.toMap(RouteDefinition::getId, r -> r.getUri().toString()));

        assertThat(routeIdToUri)
                .containsEntry("auth-route", "lb://auth-service")
                .containsEntry("tenant-route", "lb://tenant-service")
                .containsEntry("project-route", "lb://project-service")
                .containsEntry("billing-route", "lb://billing-service");
    }

    @Test
    void everyRouteHasTheRedisBackedRateLimiterAttached() {
        assertThat(gatewayProperties.getRoutes())
                .allSatisfy(route -> assertThat(route.getFilters())
                        .extracting(filter -> filter.getName())
                        .contains("RequestRateLimiter"));
    }
}
