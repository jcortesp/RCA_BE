package com.netkrow.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * - Representa el JSON que llega desde el FE para /query.
 * - Endpoint en local para ejecutar SELECT.
 * - Con @NotBlank, si viene vacío, Spring responde 400 automáticamente.
 */
public class QueryRequest {
    @NotBlank
    private String sql;

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
}
