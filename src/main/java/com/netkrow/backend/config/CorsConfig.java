package com.netkrow.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración CORS (Cross-Origin Resource Sharing).
 *
 * Para funcional:
 * - Es un tema de navegador: permite que el Frontend (otro dominio/puerto)
 *   pueda llamar al Backend sin ser bloqueado.
 *
 * Para dev:
 * - En desarrollo, si usamos Vite Proxy, el navegador ve “mismo origen”
 *   y CORS casi ni entra en juego. Igual se deja configurado por conveniencia.
 */
@Configuration
public class CorsConfig {

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${frontend.prod-url:https://netkrow-fe.vercel.app}")
    private String frontendProdUrl;

    @Value("${frontend.extra-url:https://netkrow.onrender.com}")
    private String frontendExtraUrl;

    /**
     * @Bean:
     * - Indica a Spring: “crea este objeto una vez al iniciar y mantenlo disponible”.
     * - Spring Security / Spring Web lo consumen automáticamente para aplicar CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Orígenes permitidos.
        // Nota: allowedOriginPatterns permite patrones (ej: localhost:*)
        cfg.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                frontendUrl,
                frontendProdUrl,
                frontendExtraUrl,
                "https://*.vercel.app"
        ));

        // Métodos HTTP permitidos desde el browser
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers que el FE puede enviar
        cfg.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "Accept", "Origin", "X-Requested-With"));

        // Headers que el FE puede leer explícitamente
        cfg.setExposedHeaders(List.of("Authorization", "Location"));

        // Permite credenciales/cookies si se llegaran a usar
        cfg.setAllowCredentials(true);

        // Cache del preflight (OPTIONS)
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
