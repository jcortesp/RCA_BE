package com.netkrow.backend.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RouteNode
 *
 * Para funcional:
 * - Representa un “bloque” del grafo (transaction / flow / service / server).
 * - El front mostrará en "Detalles del nodo" el objeto details (friendly).
 *
 * Para dev:
 * - details está diseñado para NO exponer dumps técnicos (flowMeta/subFlowMeta/joins).
 * - El service arma details con subFlowMeta + flowMeta (si existe).
 * - El flow arma details con flowMeta.
 * - La transaction arma details con transaction meta limpio.
 */
public class RouteNode {
    private String type;   // transaction | flow | service | server
    private String label;  // texto visible

    // Keys básicos (para ubicar en Sterling)
    private String flowName;
    private String flowKey;
    private String subFlowKey;
    private String serverKey;

    // Transaction
    private String transactionKey;

    // Detalles “amigables” para el Drawer del FE
    private Map<String, Object> details = new LinkedHashMap<>();

    public RouteNode() {}

    public RouteNode(String type, String label) {
        this.type = type;
        this.label = label;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }

    public String getFlowKey() { return flowKey; }
    public void setFlowKey(String flowKey) { this.flowKey = flowKey; }

    public String getSubFlowKey() { return subFlowKey; }
    public void setSubFlowKey(String subFlowKey) { this.subFlowKey = subFlowKey; }

    public String getServerKey() { return serverKey; }
    public void setServerKey(String serverKey) { this.serverKey = serverKey; }

    public String getTransactionKey() { return transactionKey; }
    public void setTransactionKey(String transactionKey) { this.transactionKey = transactionKey; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public void putDetail(String k, Object v) {
        if (this.details == null) this.details = new LinkedHashMap<>();
        this.details.put(k, v);
    }
}
