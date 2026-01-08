package com.netkrow.backend.repository;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class SterlingRepository {

    private static final int SNIPPET_LEN = 240;
    private static final int SNIPPET_BACK = 80;

    public String pingUser(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT USER FROM dual");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : "UNKNOWN";
        }
    }

    public List<Map<String, Object>> executeSelect(Connection conn, String sql, int maxRows) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.setMaxRows(maxRows);
            try (ResultSet rs = st.executeQuery(sql)) {
                return resultSetToList(rs);
            }
        }
    }

    // Subflows que contienen texto + match pos + snippet + auditoría subflow
    public List<Map<String, Object>> findSubFlowsContainingText(Connection conn, String searchText) throws SQLException {
        String sql =
            "WITH hits AS ( " +
            "  SELECT " +
            "    SUB_FLOW_KEY, SERVER_KEY, FLOW_KEY, " +
            "    MODIFYTS, MODIFYUSERID, MODIFYPROGID, " +
            "    CREATETS, CREATEUSERID, " +
            "    DBMS_LOB.INSTR(config_xml, ?) AS MATCH_POS, " +
            "    config_xml AS CONFIG_XML " +
            "  FROM uatconf.yfs_sub_flow " +
            "  WHERE DBMS_LOB.INSTR(config_xml, ?) > 0 " +
            ") " +
            "SELECT " +
            "  SUB_FLOW_KEY, SERVER_KEY, FLOW_KEY, " +
            "  MODIFYTS, MODIFYUSERID, MODIFYPROGID, " +
            "  CREATETS, CREATEUSERID, " +
            "  MATCH_POS, " +
            "  DBMS_LOB.SUBSTR(CONFIG_XML, ?, GREATEST(MATCH_POS - ?, 1)) AS MATCH_SNIPPET " +
            "FROM hits " +
            "ORDER BY MODIFYTS DESC";

        List<Map<String, Object>> hits = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, searchText);
            ps.setString(2, searchText);
            ps.setInt(3, SNIPPET_LEN);
            ps.setInt(4, SNIPPET_BACK);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("subFlowKey", rs.getString("SUB_FLOW_KEY"));
                    m.put("serverKey", rs.getString("SERVER_KEY"));
                    m.put("flowKey", rs.getString("FLOW_KEY"));

                    m.put("modifyTs", rs.getObject("MODIFYTS"));
                    m.put("modifyUserId", rs.getString("MODIFYUSERID"));
                    m.put("modifyProgId", rs.getString("MODIFYPROGID"));
                    m.put("createTs", rs.getObject("CREATETS"));
                    m.put("createUserId", rs.getString("CREATEUSERID"));

                    m.put("matchPos", rs.getInt("MATCH_POS"));
                    m.put("matchSnippet", rs.getString("MATCH_SNIPPET"));

                    hits.add(m);
                }
            }
        }
        return hits;
    }

    // Flow meta (incluye group name)
    public Map<String, Object> findFlowMeta(Connection conn, String flowKey) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        if (flowKey == null || flowKey.trim().isEmpty()) return result;

        String sql =
            "SELECT FLOW_NAME, OWNER_KEY, PROCESS_TYPE_KEY, FLOW_GROUP_NAME, " +
            "       CREATEUSERID, CREATETS, MODIFYTS " +
            "FROM uatconf.yfs_flow " +
            "WHERE TRIM(flow_key) = TRIM(?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flowKey.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("FLOW_NAME", rs.getString("FLOW_NAME"));
                    result.put("OWNER_KEY", rs.getString("OWNER_KEY"));
                    result.put("PROCESS_TYPE_KEY", rs.getString("PROCESS_TYPE_KEY"));
                    result.put("FLOW_GROUP_NAME", rs.getString("FLOW_GROUP_NAME"));
                    result.put("CREATEUSERID", rs.getString("CREATEUSERID"));
                    result.put("CREATETS", rs.getObject("CREATETS"));
                    result.put("MODIFYTS", rs.getObject("MODIFYTS"));
                }
            }
        }
        return result;
    }

    // Transaction list que invoca un flow (lo de antes)
    public List<Map<String, Object>> findTransactionNodeDetailed(Connection conn, String flowName) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        if (flowName == null || flowName.trim().isEmpty()) return result;

        String sql =
            "SELECT f.flow_name as FLOW_NAME, a.action_key as ACTION_KEY, a.actionname as ACTIONNAME, a.group_id as GROUP_ID, " +
            "       a.createuserid as CREATEUSERID, e.transaction_key as TRANSACTION_KEY, e.eventid as EVENTID, " +
            "       t.process_type_key as PROCESS_TYPE_KEY, t.owner_key as OWNER_KEY, t.listener_type as LISTENER_TYPE " +
            "FROM uatconf.yfs_action a " +
            "INNER JOIN uatconf.yfs_invoked_flows i ON a.action_key = i.action_key " +
            "INNER JOIN uatconf.yfs_flow f ON i.flow_key = f.flow_key " +
            "INNER JOIN uatconf.yfs_event_condition ec ON i.action_key = ec.action_key " +
            "INNER JOIN uatconf.yfs_event e ON ec.event_key = e.event_key " +
            "INNER JOIN uatconf.yfs_transaction t ON e.transaction_key = t.transaction_key " +
            "WHERE TRIM(f.flow_name) = TRIM(?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flowName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> txNode = new LinkedHashMap<>();
                    txNode.put("FLOW_NAME", rs.getString("FLOW_NAME"));
                    txNode.put("ACTION_KEY", rs.getString("ACTION_KEY"));
                    txNode.put("ACTIONNAME", rs.getString("ACTIONNAME"));
                    txNode.put("GROUP_ID", rs.getString("GROUP_ID"));
                    txNode.put("CREATEUSERID", rs.getString("CREATEUSERID"));
                    txNode.put("TRANSACTION_KEY", rs.getString("TRANSACTION_KEY"));
                    txNode.put("EVENTID", rs.getString("EVENTID"));
                    txNode.put("PROCESS_TYPE_KEY", rs.getString("PROCESS_TYPE_KEY"));
                    txNode.put("OWNER_KEY", rs.getString("OWNER_KEY"));
                    txNode.put("LISTENER_TYPE", rs.getString("LISTENER_TYPE"));
                    result.add(txNode);
                }
            }
        }
        return result;
    }

    /**
     * ✅ NUEVO: meta “friendly” de una transaction (para Drawer)
     *
     * Querido: serverKey, transactionKey, transactionName, owner, createUserId, createTs, modifyTs
     *
     * Nota:
     * - "transactionName" puede no existir en tu tabla; muchos Sterling solo tienen TRANSACTION_KEY.
     *   Si no existe, devolvemos transactionName = TRANSACTION_KEY.
     */
    public Map<String, Object> findTransactionMeta(Connection conn, String transactionKey) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        if (transactionKey == null || transactionKey.trim().isEmpty()) return m;

        // Si tu schema tiene TRANSACTION_NAME, cámbialo aquí.
        // Si no existe, deja transaction_name como alias del key.
        String sql =
            "SELECT " +
            "  t.transaction_key AS TRANSACTION_KEY, " +
            "  t.owner_key       AS OWNER_KEY, " +
            "  t.createuserid    AS CREATEUSERID, " +
            "  t.createts        AS CREATETS, " +
            "  t.modifyts        AS MODIFYTS " +
            "FROM uatconf.yfs_transaction t " +
            "WHERE TRIM(t.transaction_key) = TRIM(?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionKey.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.put("transactionKey", rs.getString("TRANSACTION_KEY"));
                    m.put("transactionName", rs.getString("TRANSACTION_KEY")); // fallback
                    m.put("owner", rs.getString("OWNER_KEY"));
                    m.put("createUserId", rs.getString("CREATEUSERID"));
                    m.put("createTs", rs.getObject("CREATETS"));
                    m.put("modifyTs", rs.getObject("MODIFYTS"));
                }
            }
        }
        return m;
    }

    // Helpers
    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}
