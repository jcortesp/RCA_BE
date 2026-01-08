package com.netkrow.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Input del endpoint /backtrace.
 *
 * Para funcional:
 * - search = el texto a buscar (service/flow/keyword).
 * - maxDepth = hasta qué nivel “subimos” por padres (protección para no loops infinitos).
 */
public class BacktraceRequest {

    @NotBlank
    private String search;

    // Opcional: si no viene, usamos default (15).
    private Integer maxDepth;

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public Integer getMaxDepth() { return maxDepth; }
    public void setMaxDepth(Integer maxDepth) { this.maxDepth = maxDepth; }
}
