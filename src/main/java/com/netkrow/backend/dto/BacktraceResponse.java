package com.netkrow.backend.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta del backtrace.
 *
 * - mode:
 *   Indica si la respuesta es REAL (Oracle) o DEMO (sin conexión a BD).
 *
 * - routes:
 *   Lista de rutas posibles de invocación.
 *   Cada ruta es una lista ordenada de nodos desde un root hasta el servicio buscado.
 *
 * - transactions:
 *   Información adicional de transacciones detectadas nodo inicial.
 */
public class BacktraceResponse {

    // Valores esperados: "DEMO" | "REAL"
    public static final String MODE_DEMO = "DEMO";
    public static final String MODE_REAL = "REAL";

    private String mode;
    private List<List<RouteNode>> routes = new ArrayList<>();
    private List<TransactionInfo> transactions = new ArrayList<>();

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public List<List<RouteNode>> getRoutes() { return routes; }
    public void setRoutes(List<List<RouteNode>> routes) { this.routes = routes; }

    public List<TransactionInfo> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionInfo> transactions) { this.transactions = transactions; }
}
