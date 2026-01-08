package com.netkrow.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración CORS (Cross-Origin Resource Sharing).
 *
 * - Permite que el Frontend (otro origen o puerto) pueda llamar al Backend sin que el navegador lo bloquee.
 *
 * - En local, Vite usa un proxy y el navegador ve “mismo origen”.
 * - Se dejua esta configuración lista para cuando no haya proxy o cuando se requeira subir a la web.
 */
@Configuration
public class CorsConfig {

    /**
     * Origen local del FE.
     */
    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Origen del FE cuando se requiera subir a la web
     */
    @Value("${frontend.prod-url:https://netkrow-fe.vercel.app}")
    private String frontendProdUrl;

    /**
     * Permite o no el patrón "http://localhost:*".
     * Útil cuando se cambia de puerto o para pruebas con otro FE local.
     *
     * - Local: true
     * - Web: false
     */
    @Value("${cors.allow-localhost-wildcard:true}")
    private boolean allowLocalhostWildcard;

    /**
     * - Spring crea este objeto una vez al iniciar y lo mantiene disponible para aplicar CORS globalmente.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // =========================
        // ORÍGENES PERMITIDOS
        // =========================
        //
        // Se permiten los orígenes conocidos del FE.
        // Agregar aquí si se necesitan permitir más dominios.
        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add(frontendUrl);
        allowedOrigins.add(frontendProdUrl);

        // En LOCAL se permite cualquier puerto de localhost.
        // En WEB debe quedar en false.
        if (allowLocalhostWildcard) {
            // Para patrones o Wildcards (*) se usa allowedOriginPatterns.
            cfg.setAllowedOriginPatterns(List.of("http://localhost:*"));
        }

        // allowedOrigins es lo más seguro para WEB.
        cfg.setAllowedOrigins(allowedOrigins);

        // =========================
        // MÉTODOS HTTP
        // =========================
        cfg.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));

        // =========================
        // HEADERS que el FE puede enviar
        // =========================
        cfg.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept"));

        // Permite envío de credenciales (ejemplo: Authorization header)
        cfg.setAllowCredentials(true);

        // Cache de preflight (se deja para cuando se quiera subir a la WEB)
        cfg.setMaxAge(3600L);

        // Aplicar la config a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
