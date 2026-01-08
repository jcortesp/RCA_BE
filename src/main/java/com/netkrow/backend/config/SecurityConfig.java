package com.netkrow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security (placeholder por ahora, no se requiere en LOCAL pero si cuando se quiera subir a la WEB).
 * - En este MVP no hay login, ni usuarios ni roles.
 * - Se deja esto para futuro (JWT/SSO/roles).
 * - Por ahora permitAll().
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF: Protege escenarios con cookies/sesiones
        // En APIs “stateless” se deshabilita.
        http
            .csrf(csrf -> csrf.disable())

            // CORS se aplica con CorsConfig (CorsConfigurationSource)
            .cors(cors -> {})

            // Todo permitido (MVP en local)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            // Sin sesiones
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
