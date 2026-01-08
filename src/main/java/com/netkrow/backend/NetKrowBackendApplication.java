package com.netkrow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend (Spring Boot).
 *
 * - @SpringBootApplication habilita:
 *   - Auto-configuración de Spring Boot
 *   - Configuración de la app
 */
@SpringBootApplication
public class NetKrowBackendApplication {

    /**
     * Arranca la aplicación Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(NetKrowBackendApplication.class, args);
    }
}
