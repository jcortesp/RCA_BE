package com.netkrow.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * search = el texto a buscar que representa un servicio o flujo de OMNI
 * maxDepth = limite en la busqueda de padres para evitar loops infinitos.
 */
public class BacktraceRequest {

    @NotBlank
    private String search;

    // Default 15.
    private Integer maxDepth;

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public Integer getMaxDepth() { return maxDepth; }
    public void setMaxDepth(Integer maxDepth) { this.maxDepth = maxDepth; }
}
