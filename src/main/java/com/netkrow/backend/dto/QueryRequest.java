package com.netkrow.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object):
 * - Representa el JSON que llega desde el FE para /query.
 *
 * Para consultor funcional:
 * - Este endpoint es “herramienta técnica” local para ejecutar SELECTs.
 *
 * Para dev:
 * - Con @NotBlank, si viene vacío, Spring responde 400 automáticamente.
 */
public class QueryRequest {
    @NotBlank
    private String sql;

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
}
