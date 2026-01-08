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
 * - Implementa el algoritmo de "backtrace" sobre OMNI buscando rutas posibles desde un “root” hasta el servicio buscado.
 * retorna:
 * - routes: lista de rutas (cada ruta es una lista ordenada de RouteNode):
 *   Transaction -> Flow -> Service (search)
 * - transactions: lista de TransactionInfo.
 */
@Service
public class RcaService {

    /**
     * Repositorio de queries a tablauatconf.
     * Aquí está toda la lectura SQL, el service se enfoca en el algoritmo y armado de rutas.
     */
    private final SterlingRepository repo;

    public RcaService(SterlingRepository repo) {
        this.repo = repo;
    }

    /**
     * Ejecuta backtrace:
     * - search: servicio a rastrear en config_xml.
     * - maxDepth: profundidad máxima de búsqueda hacia padres (evita loops).
     * - maxRoutes: número máximo de rutas a devolver (evita payloads enormes al FE).
     *
     * Flujo general:
     * 1) Encuentra subflows cuyo config_xml contiene 'search'.
     * 2) Por cada hit, construye el nodo "service".
     * 3) Recorre recursivamente hacia arriba:
     *    - agrega el nodo "flow"
     *    - busca transactions que invocan ese flowName y arma rutas completas.
     *    - busca subflows “padres” que contengan el flowName para seguir subiendo.
     */
    public BacktraceResponse backtrace(Connection conn, String search, int maxDepth, int maxRoutes) throws SQLException {
        BacktraceResponse resp = new BacktraceResponse();
        
        // Modo REAL: se conectó a Oracle y se ejecutó el algoritmo.

        resp.setMode("REAL");
        
        // Salida: rutas completas y lista de transactions.
        List<List<RouteNode>> routesOut = new ArrayList<>();
        List<TransactionInfo> txOut = new ArrayList<>();

        // 1) Hits iniciales: subflows que contienen el texto buscado dentro del CLOB config_xml.
        List<Map<String, Object>> hits = repo.findSubFlowsContainingText(conn, search);

        // Si no hay hits, no hay rutas posibles.
        if (hits.isEmpty()) {
            resp.setRoutes(List.of());
            resp.setTransactions(List.of());
            return resp;
        }

        // Evita loops o recorridos repetidos durante la recursión.
        Set<String> visited = new HashSet<>();

        // Se recorre cada hit (subflow cuyo XML contiene search).
        for (Map<String, Object> hit : hits) {
            if (routesOut.size() >= maxRoutes) break;

            // Identificadores base del hit.
            String flowKey = s(hit.get("flowKey"));
            String serverKey = s(hit.get("serverKey"));
            String subFlowKey = s(hit.get("subFlowKey"));

            // Flow meta (para flowName / flowGroupName)
            Map<String, Object> flowMeta = repo.findFlowMeta(conn, flowKey);
            String flowName = s(flowMeta.get("FLOW_NAME"));
            String flowGroupName = s(flowMeta.get("FLOW_GROUP_NAME"));

            // service node = el texto buscado
            RouteNode serviceNode = new RouteNode("service", search);
            serviceNode.setFlowKey(flowKey);
            serviceNode.setServerKey(serverKey);
            serviceNode.setSubFlowKey(subFlowKey);
            serviceNode.setFlowName(flowName);

            // details para service (usa subFlowMeta + flowMeta)
            serviceNode.setDetails(buildServiceOrFlowDetails(flowName, flowKey, serverKey, flowGroupName));

            // Path: empieza desde abajo (service), luego se van agregando parents (flow, transaction)
            LinkedList<RouteNode> path = new LinkedList<>();
            path.add(serviceNode);

            // SubFlow meta (source real del service)
            // serviceNode.putDetail("matchPos", hit.get("matchPos"));
            // serviceNode.putDetail("matchSnippet", hit.get("matchSnippet"));

            findParentsRecursive(conn, flowKey, serverKey, subFlowKey, path, routesOut, txOut, visited, 0, maxDepth, maxRoutes);

            if (routesOut.size() >= maxRoutes) break;
        }

        // Salida final
        resp.setRoutes(routesOut);
        resp.setTransactions(txOut);
        return resp;
    }

    /**
     * Recorrido recursivo hacia padres:
     * - Agrega un nodo FLOW al inicio del path.
     * - Busca TXs que invocan el flowName y arma rutas completas TX -> FLOW -> ... -> SERVICE.
     * - Busca subflows “padres” cuyo XML contiene flowName y continúa recursión.
     *
     * - maxDepth: corta la recursión.
     * - maxRoutes: corta si ya se juntaron suficientes rutas.
     * - visited: evita loops / repetición.
     */
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

        // No exceder maxRoutes
        if (routesOut.size() >= maxRoutes) return;

        // No exceder maxDepth
        if (depth >= maxDepth) return;

        /**
         * - Identifica el estado actual de la recursión.
         */
        String visitKey = flowKey + "|" + serverKey + "|" + subFlowKey + "|" + depth;
        if (visited.contains(visitKey)) return;
        visited.add(visitKey);

        // Flow meta (padre actual)
        Map<String, Object> flowMeta = repo.findFlowMeta(conn, flowKey);
        String flowName = s(flowMeta.get("FLOW_NAME"));
        String flowGroupName = s(flowMeta.get("FLOW_GROUP_NAME"));

        // Flow node (padre)
        RouteNode flowNode = new RouteNode("flow", flowName.isBlank() ? flowKey : flowName);
        flowNode.setFlowName(flowName);
        flowNode.setFlowKey(flowKey);
        flowNode.setServerKey(serverKey);
        flowNode.setSubFlowKey(subFlowKey);

        // details para flow (usa flowMeta)
        flowNode.setDetails(buildServiceOrFlowDetails(flowName, flowKey, serverKey, flowGroupName));

        // Se inserta flow antes del service (para TX -> FLOW -> SERVICE)
        currentPath.addFirst(flowNode);

        /**
         * 1) Transactions que invocan este flowName:
         * - Si hay flowName, buscamos transacciones que lo invocan.
         * - Por cada TX encontrada creamos un nodo root "transaction" y armamos una ruta completa TX -> (FLOW -> ... -> SERVICE)
         */
        if (!flowName.isBlank()) {
            List<Map<String, Object>> txRows = repo.findTransactionNodeDetailed(conn, flowName);

            for (Map<String, Object> tx : txRows) {
                if (routesOut.size() >= maxRoutes) break;

                String txKey = s(tx.get("TRANSACTION_KEY"));

                // Transaction node (root)
                RouteNode txNode = new RouteNode("transaction", txKey);
                txNode.setTransactionKey(txKey);
                txNode.setServerKey(serverKey); 

                // Cargar meta de transaction
                Map<String, Object> txMeta = repo.findTransactionMeta(conn, txKey);
                txNode.setDetails(buildTransactionDetails(serverKey, txMeta));

                // Llenar TransactionInfo
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

        /**
         * Cleanup:
         * - Removemos el FLOW que se agregó al inicio, para restaurar el path al estado previo.
         * - Esto evita que el FLOW “se acumule” cuando se regresa de la recursión.
         */
        if (!currentPath.isEmpty() && "flow".equals(currentPath.getFirst().getType())) {
            currentPath.removeFirst();
        }
    }

    // -------------------------
    // builders para FE
    // -------------------------

     /**
     * Details para nodos service/flow.
     * Mantiene un conjunto pequeño y estable de metadatos que el FE puede mostrar.
     */
    private Map<String, Object> buildServiceOrFlowDetails(String flowName, String flowKey, String serverKey, String flowGroupName) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("flowName", blankToNull(flowName));
        d.put("flowKey", blankToNull(flowKey));
        d.put("serverKey", blankToNull(serverKey));
        d.put("flowGroupName", blankToNull(flowGroupName));
        return d;
    }
    // Details para nodo transaction.
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

    // helpers
    private static String s(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    // Normaliza String
    private static String blankToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
