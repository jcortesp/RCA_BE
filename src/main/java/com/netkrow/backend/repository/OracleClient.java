package com.netkrow.backend.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * - Esta clase solo abre conexión a la base de datos Oracle.
 * - No contiene lógica de OMNI.
 * - Se deja en MVP con DriverManager.
 */
@Component
public class OracleClient {

    @Value("${oracle.datasource.url:}")
    private String oracleUrl;

    @Value("${oracle.datasource.username:}")
    private String oracleUser;

    @Value("${oracle.datasource.password:}")
    private String oraclePassword;

    public boolean isOracleConfigured() {
        return oracleUrl != null && !oracleUrl.isBlank()
            && oracleUser != null && !oracleUser.isBlank()
            && oraclePassword != null && !oraclePassword.isBlank();
    }

    /**
     * Abre conexión JDBC a Oracle.
     * - Cada request abre y cierra.
     */
    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(oracleUrl, oracleUser, oraclePassword);
    }
}
