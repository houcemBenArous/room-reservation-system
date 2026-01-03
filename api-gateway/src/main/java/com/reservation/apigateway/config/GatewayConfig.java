package com.reservation.apigateway.config;



import com.reservation.apigateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - Pas de filtre JWT
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://auth-service"))

                // Agent Service - Avec filtre JWT
                .route("agent-service", r -> r
                        .path("/api/agent/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://agent-service"))

                // Room Service - Avec filtre JWT
                .route("room-service", r -> r
                        .path("/api/rooms/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://room-service"))

                // Reservation Service - Avec filtre JWT
                .route("reservation-service", r -> r
                        .path("/api/reservations/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://reservation-service"))

                .build();
    }
}
