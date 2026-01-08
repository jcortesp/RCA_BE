package com.netkrow.backend.service;

import com.netkrow.backend.dto.BacktraceResponse;
import com.netkrow.backend.dto.RouteNode;
import com.netkrow.backend.dto.TransactionInfo;
import com.netkrow.backend.repository.SterlingRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * RcaService
 *
 * Para funcional:
 * - Implementa backtrace: rutas padres desde Sterling config.
 *
 * Para dev:
 * - Construye RouteNode.details “friendly” según type.
 */
@Service
public class RcaService {

    private final SterlingRepository repo;

    public RcaService(SterlingRepository repo) {
        this.repo = repo;
    }

    public BacktraceResponse backtrace(Connection conn, String search, int maxDepth, int maxRoutes) throws SQLException {
        BacktraceResponse resp = new BacktraceResponse();
        resp.setMode("REAL");

        List<List<RouteNode>> routesOut = new ArrayList<>();
        List<TransactionInfo> txOut = new ArrayList<>();

        // 1) Hits iniciales: subflows que contienen el texto buscado
        List<Map<String, Object>> hits = repo.findSubFlowsContainingText(conn, search);
        if (hits.isEmpty()) {
            resp.setRoutes(List.of());
            resp.setTransactions(List.of());
            return resp;
        }

        Set<String> visited = new HashSet<>();

        for (Map<String, Object> hit : hits) {
            if (routesOut.size() >= maxRoutes) break;

            String flowKey = s(hit.get("flowKey"));
            String serverKey = s(hit.get("serverKey"));
            String subFlowKey = s(hit.get("subFlowKey"));

            // Flow meta (para flowName / flowGroupName)
            Map<String, Object> flowMeta = repo.findFlowMeta(conn, flowKey);
            String flowName = s(flowMeta.get("FLOW_NAME"));
            String flowGroupName = s(flowMeta.get("FLOW_GROUP_NAME"));

            // ✅ service node = el texto buscado
            RouteNode serviceNode = new RouteNode("service", search);
            serviceNode.setFlowKey(flowKey);
            serviceNode.setServerKey(serverKey);
            serviceNode.setSubFlowKey(subFlowKey);
            serviceNode.setFlowName(flowName);

            // details friendly para service (usa subFlowMeta + flowMeta)
            serviceNode.setDetails(buildServiceOrFlowDetails(flowName, flowKey, serverKey, flowGroupName));

            // Path: empezamos desde abajo (service), luego iremos agregando parents (flow, transaction)
            LinkedList<RouteNode> path = new LinkedList<>();
            path.add(serviceNode);

            // SubFlow meta (source real del service, aunque no se muestre “crudo”)
            // Si luego quieres mostrar matchSnippet, lo puedes agregar aquí sin exponer todo:
            // serviceNode.putDetail("matchPos", hit.get("matchPos"));
            // serviceNode.putDetail("matchSnippet", hit.get("matchSnippet"));

            findParentsRecursive(conn, flowKey, serverKey, subFlowKey, path, routesOut, txOut, visited, 0, maxDepth, maxRoutes);

            if (routesOut.size() >= maxRoutes) break;
        }

        resp.setRoutes(routesOut);
        resp.setTransactions(txOut);
        return resp;
    }

    private void findParentsRecursive(
        Connection conn,
        String flowKey,
        String serverKey,
        String subFlowKey,
        LinkedList<RouteNode> currentPath,
        List<List<RouteNode>> routesOut,
        List<TransactionInfo> txOut,
        Set<String> visited,
        int depth,
        int maxDepth,
        int maxRoutes
    ) throws SQLException {

        if (routesOut.size() >= maxRoutes) return;
        if (depth >= maxDepth) return;

        String visitKey = flowKey + "|" + serverKey + "|" + subFlowKey + "|" + depth;
        if (visited.contains(visitKey)) return;
        visited.add(visitKey);

        // Flow meta (padre actual)
        Map<String, Object> flowMeta = repo.findFlowMeta(conn, flowKey);
        String flowName = s(flowMeta.get("FLOW_NAME"));
        String flowGroupName = s(flowMeta.get("FLOW_GROUP_NAME"));

        // ✅ Flow node (padre)
        RouteNode flowNode = new RouteNode("flow", flowName.isBlank() ? flowKey : flowName);
        flowNode.setFlowName(flowName);
        flowNode.setFlowKey(flowKey);
        flowNode.setServerKey(serverKey);
        flowNode.setSubFlowKey(subFlowKey);

        // details friendly para flow (usa flowMeta)
        flowNode.setDetails(buildServiceOrFlowDetails(flowName, flowKey, serverKey, flowGroupName));

        // Insertamos flow antes del service (para que se vea TX -> FLOW -> SERVICE)
        currentPath.addFirst(flowNode);

        // 1) Transactions que invocan este flowName
        if (!flowName.isBlank()) {
            List<Map<String, Object>> txRows = repo.findTransactionNodeDetailed(conn, flowName);

            for (Map<String, Object> tx : txRows) {
                if (routesOut.size() >= maxRoutes) break;

                String txKey = s(tx.get("TRANSACTION_KEY"));

                // ✅ Transaction node (root)
                RouteNode txNode = new RouteNode("transaction", txKey);
                txNode.setTransactionKey(txKey);
                txNode.setServerKey(serverKey); // la quieres visible; si no aplica, igual sirve para contexto

                // Cargar meta limpia de transaction (createTs/modifyTs/etc.)
                Map<String, Object> txMeta = repo.findTransactionMeta(conn, txKey);
                txNode.setDetails(buildTransactionDetails(serverKey, txMeta));

                // También llenar TransactionInfo (si la usas en otro panel/lista)
                TransactionInfo ti = new TransactionInfo();
                ti.setTransactionKey(txKey);
                ti.setFlowName(flowName);
                txOut.add(ti);

                LinkedList<RouteNode> route = new LinkedList<>();
                route.add(txNode);
                route.addAll(currentPath);
                routesOut.add(route);
            }
        } else {
            // Si no hay flowName, la ruta termina aquí
            routesOut.add(new LinkedList<>(currentPath));
        }

        // 2) Buscar subflows padres (config_xml contiene flowName)
        if (!flowName.isBlank()) {
            List<Map<String, Object>> parentHits = repo.findSubFlowsContainingText(conn, flowName);

            for (Map<String, Object> ph : parentHits) {
                if (routesOut.size() >= maxRoutes) break;

                String parentFlowKey = s(ph.get("flowKey"));
                String parentServerKey = s(ph.get("serverKey"));
                String parentSubFlowKey = s(ph.get("subFlowKey"));

                LinkedList<RouteNode> newPath = new LinkedList<>(currentPath);
                findParentsRecursive(conn, parentFlowKey, parentServerKey, parentSubFlowKey,
                    newPath, routesOut, txOut, visited, depth + 1, maxDepth, maxRoutes);
            }
        }

        // Cleanup: removemos el flowNode agregado
        if (!currentPath.isEmpty() && "flow".equals(currentPath.getFirst().getType())) {
            currentPath.removeFirst();
        }
    }

    // -------------------------
    // builders friendly (lo que quieres ver en FE)
    // -------------------------

    private Map<String, Object> buildServiceOrFlowDetails(String flowName, String flowKey, String serverKey, String flowGroupName) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("flowName", blankToNull(flowName));
        d.put("flowKey", blankToNull(flowKey));
        d.put("serverKey", blankToNull(serverKey));
        d.put("flowGroupName", blankToNull(flowGroupName));
        return d;
    }

    private Map<String, Object> buildTransactionDetails(String serverKey, Map<String, Object> txMeta) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("serverKey", blankToNull(serverKey));
        d.put("transactionKey", blankToNull(s(txMeta.get("transactionKey"))));
        d.put("transactionName", blankToNull(s(txMeta.get("transactionName"))));
        d.put("owner", blankToNull(s(txMeta.get("owner"))));
        d.put("createUserId", blankToNull(s(txMeta.get("createUserId"))));
        d.put("createTs", txMeta.get("createTs"));
        d.put("modifyTs", txMeta.get("modifyTs"));
        return d;
    }

    private static String s(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static String blankToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
