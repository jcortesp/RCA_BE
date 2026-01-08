package com.netkrow.backend.dto;

/**
 * - Metadatos del subflow que contiene el texto buscado
 * - También incluye matchPos/matchSnippet (por qué apareció)
 * - Se arma desde uatconf.yfs_sub_flow (CLOB config_xml)
 */
public class SubFlowMeta {

    private String subFlowKey;
    private String serverKey;
    private String flowKey;

    private Object modifyTs;
    private String modifyUserId;
    private String modifyProgId;

    private Object createTs;
    private String createUserId;

    private Integer matchPos; // Posición del match
    private String matchSnippet; // Fragmento que causó el match

    public String getSubFlowKey() { return subFlowKey; }
    public void setSubFlowKey(String subFlowKey) { this.subFlowKey = subFlowKey; }

    public String getServerKey() { return serverKey; }
    public void setServerKey(String serverKey) { this.serverKey = serverKey; }

    public String getFlowKey() { return flowKey; }
    public void setFlowKey(String flowKey) { this.flowKey = flowKey; }

    public Object getModifyTs() { return modifyTs; }
    public void setModifyTs(Object modifyTs) { this.modifyTs = modifyTs; }

    public String getModifyUserId() { return modifyUserId; }
    public void setModifyUserId(String modifyUserId) { this.modifyUserId = modifyUserId; }

    public String getModifyProgId() { return modifyProgId; }
    public void setModifyProgId(String modifyProgId) { this.modifyProgId = modifyProgId; }

    public Object getCreateTs() { return createTs; }
    public void setCreateTs(Object createTs) { this.createTs = createTs; }

    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }

    public Integer getMatchPos() { return matchPos; }
    public void setMatchPos(Integer matchPos) { this.matchPos = matchPos; }

    public String getMatchSnippet() { return matchSnippet; }
    public void setMatchSnippet(String matchSnippet) { this.matchSnippet = matchSnippet; }
}
