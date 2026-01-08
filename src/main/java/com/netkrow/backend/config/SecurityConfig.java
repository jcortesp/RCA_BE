package com.netkrow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security (placeholder).
 *
 * Para funcional:
 * - Hoy NO hay login, NO hay usuarios, NO hay roles.
 * - El MVP está pensado para uso local / equipo.
 *
 * Para dev:
 * - Se deja esto para futuro cuando se “publique” (JWT/SSO/roles).
 * - Por ahora permitAll() para evitar fricción.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF:
        // - Protege escenarios con cookies/sesión (web tradicional).
        // - En APIs tipo “stateless” se suele deshabilitar.
        http
            .csrf(csrf -> csrf.disable())

            // CORS se aplica usando CorsConfig
            .cors(cors -> {})

            // Reglas: todo permitido (MVP local)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/rca/**").permitAll()
                .anyRequest().permitAll()
            )

            // Sin sesiones
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
