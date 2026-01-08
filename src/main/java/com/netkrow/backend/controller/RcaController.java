package com.netkrow.backend.controller;

import com.netkrow.backend.dto.*;
import com.netkrow.backend.repository.OracleClient;
import com.netkrow.backend.repository.SterlingRepository;
import com.netkrow.backend.service.RcaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * RcaController (API)
 *
 * Para consultor funcional:
 * - Este archivo define “qué endpoints existen” y qué reciben/devuelven.
 * - No implementa el algoritmo; solo orquesta.
 *
 * Para dev:
 * - Controller debe ser delgado: valida, llama service/repo y retorna.
 */
@RestController
@RequestMapping("/api/rca")
public class RcaController {

    private final OracleClient oracle;
    private final SterlingRepository repo;
    private final RcaService service;

    @Value("${rca.demo:false}")
    private boolean demoMode;

    public RcaController(OracleClient oracle, SterlingRepository repo, RcaService service) {
        this.oracle = oracle;
        this.repo = repo;
        this.service = service;
    }

    /**
     * MVP helper:
     * - Si no hay Oracle configurado y no estamos en demo, devolvemos 503.
     */
    private void ensureOracleConfiguredOrDemo() {
        if (!oracle.isOracleConfigured() && !demoMode) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Oracle no está configurado (oracle.datasource.*) y rca.demo=false"
            );
        }
    }

    /**
     * GET /api/rca/ping
     */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        if (demoMode) {
            return Map.of("mode", "DEMO", "oracle", "SKIPPED");
        }
        if (!oracle.isOracleConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Oracle no configurado.");
        }

        try (Connection conn = oracle.openConnection()) {
            String user = repo.pingUser(conn);
            return Map.of("ok", true, "user", user);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Oracle error: " + e.getMessage(), e);
        }
    }

    /**
     * POST /api/rca/query
     */
    @PostMapping("/query")
    public List<Map<String, Object>> query(@Valid @RequestBody QueryRequest req) {
        ensureOracleConfiguredOrDemo();

        String sql = req.getSql().trim();
        if (!sql.toLowerCase().startsWith("select")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten queries SELECT");
        }

        if (demoMode || !oracle.isOracleConfigured()) {
            return List.of(Map.of("DEMO", true, "sql", sql));
        }

        try (Connection conn = oracle.openConnection()) {
            return repo.executeSelect(conn, sql, 50);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Oracle error: " + e.getMessage(), e);
        }
    }

    /**
     * POST /api/rca/backtrace
     *
     * Para funcional:
     * - search: texto a rastrear.
     * - El resultado son rutas posibles “desde un root” hasta tu search.
     *
     * Para dev:
     * - maxDepth default 15
     * - maxRoutes 60
     */
    @PostMapping("/backtrace")
    public BacktraceResponse backtrace(@Valid @RequestBody BacktraceRequest req) {
        ensureOracleConfiguredOrDemo();

        String search = req.getSearch().trim();
        int maxDepth = (req.getMaxDepth() == null ? 15 : req.getMaxDepth());
        int maxRoutes = 60;

        // DEMO: no Oracle, respuesta simulada para probar UI/flujo
        if (demoMode || !oracle.isOracleConfigured()) {
            RouteNode tx = new RouteNode("transaction", "SHIPMENT_INVOICE");
            tx.setTransactionKey("SHIPMENT_INVOICE");

            RouteNode flow = new RouteNode("flow", "OrderDelivery");
            flow.setFlowName("OrderDelivery");

            RouteNode svc = new RouteNode("service", search);

            BacktraceResponse resp = new BacktraceResponse();
            resp.setMode("DEMO");
            resp.setRoutes(List.of(List.of(tx, flow, svc)));

            TransactionInfo t = new TransactionInfo();
            t.setTransactionKey("SHIPMENT_INVOICE");
            t.setFlowName("OrderDelivery");
            resp.setTransactions(List.of(t));

            return resp;
        }

        // REAL: Oracle + algoritmo
        try (Connection conn = oracle.openConnection()) {
            return service.backtrace(conn, search, maxDepth, maxRoutes);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Oracle error: " + e.getMessage(), e);
        }
    }
}
