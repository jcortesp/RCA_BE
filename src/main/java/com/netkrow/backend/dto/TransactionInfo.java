package com.netkrow.backend.dto;

/**
 * Datos de una Transaction encontrada como “root”
 * - Se arma con joins entre tablas de OMNI:
 *   yfs_action -> yfs_invoked_flows -> yfs_flow -> yfs_event_condition -> yfs_event -> yfs_transaction
 * - “Transaction” suele ser el punto donde un evento dispara un flujo, esto ayuda a entender desde dónde se inicia una ejecución.
 * - Este DTO representa 1 fila del query findTransactionNodeDetailed().
  */
public class TransactionInfo {

    // Flow que está siendo invocado por la transaction (f.flow_name)
    private String flowName;

    // Acción (yfs_action)
    private String actionKey;
    private String actionName;
    private String groupId;
    private String createUserId;

    // Evento / Transaction (yfs_event, yfs_transaction)
    private String transactionKey;
    private String eventId;

    // Metadata de transaction (yfs_transaction)
    private String processTypeKey;
    private String ownerKey;
    private String listenerType;

    public TransactionInfo() {}

    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }

    public String getActionKey() { return actionKey; }
    public void setActionKey(String actionKey) { this.actionKey = actionKey; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }

    public String getTransactionKey() { return transactionKey; }
    public void setTransactionKey(String transactionKey) { this.transactionKey = transactionKey; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getProcessTypeKey() { return processTypeKey; }
    public void setProcessTypeKey(String processTypeKey) { this.processTypeKey = processTypeKey; }

    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }

    public String getListenerType() { return listenerType; }
    public void setListenerType(String listenerType) { this.listenerType = listenerType; }

    @Override
    public String toString() {
        return "TransactionInfo{" +
            "flowName='" + flowName + '\'' +
            ", actionKey='" + actionKey + '\'' +
            ", actionName='" + actionName + '\'' +
            ", groupId='" + groupId + '\'' +
            ", createUserId='" + createUserId + '\'' +
            ", transactionKey='" + transactionKey + '\'' +
            ", eventId='" + eventId + '\'' +
            ", processTypeKey='" + processTypeKey + '\'' +
            ", ownerKey='" + ownerKey + '\'' +
            ", listenerType='" + listenerType + '\'' +
            '}';
    }
}
