package com.netkrow.backend.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de /backtrace.
 *
 * Para funcional:
 * - routes: “caminos” posibles de invocación (de root a tu service/flow).
 * - transactions: detalles si se encontró una transaction que dispare la ejecución.
 * - mode: DEMO (sin Oracle) o REAL.
 *
 * Para dev:
 * - Esta estructura es estable para el Frontend (ReactFlow).
 */
public class BacktraceResponse {
    private String mode; // DEMO o REAL
    private List<List<RouteNode>> routes = new ArrayList<>();
    private List<TransactionInfo> transactions = new ArrayList<>();

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public List<List<RouteNode>> getRoutes() { return routes; }
    public void setRoutes(List<List<RouteNode>> routes) { this.routes = routes; }

    public List<TransactionInfo> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionInfo> transactions) { this.transactions = transactions; }
}
