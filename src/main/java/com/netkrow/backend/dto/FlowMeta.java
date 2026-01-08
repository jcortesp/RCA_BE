package com.netkrow.backend.dto;

/**
 * FlowMeta
 *
 * Para funcional:
 * - Metadatos del flow (nombre, grupo, proceso, owner)
 *
 * Para dev:
 * - Se arma desde uatconf.yfs_flow
 */
public class FlowMeta {

    private String flowName;
    private String ownerKey;
    private String processTypeKey;
    private String flowGroupName;

    private String createUserId;
    private Object createTs;   // Timestamp/Date (MVP)
    private Object modifyTs;

    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }

    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }

    public String getProcessTypeKey() { return processTypeKey; }
    public void setProcessTypeKey(String processTypeKey) { this.processTypeKey = processTypeKey; }

    public String getFlowGroupName() { return flowGroupName; }
    public void setFlowGroupName(String flowGroupName) { this.flowGroupName = flowGroupName; }

    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }

    public Object getCreateTs() { return createTs; }
    public void setCreateTs(Object createTs) { this.createTs = createTs; }

    public Object getModifyTs() { return modifyTs; }
    public void setModifyTs(Object modifyTs) { this.modifyTs = modifyTs; }
}
