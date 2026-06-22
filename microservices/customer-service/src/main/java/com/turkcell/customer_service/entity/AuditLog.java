package com.turkcell.customer_service.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "table_name", length = 50, nullable = false)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    /** INSERT | UPDATE | DELETE */
    @Column(name = "action", length = 10, nullable = false)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data", columnDefinition = "jsonb")
    private Map<String, Object> oldData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", columnDefinition = "jsonb")
    private Map<String, Object> newData;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------
    public AuditLog() {}

    public AuditLog(UUID id, String tableName, UUID recordId, String action,
                    Map<String, Object> oldData, Map<String, Object> newData,
                    String changedBy, OffsetDateTime changedAt) {
        this.id = id;
        this.tableName = tableName;
        this.recordId = recordId;
        this.action = action;
        this.oldData = oldData;
        this.newData = newData;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public UUID getRecordId() { return recordId; }
    public void setRecordId(UUID recordId) { this.recordId = recordId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Map<String, Object> getOldData() { return oldData; }
    public void setOldData(Map<String, Object> oldData) { this.oldData = oldData; }

    public Map<String, Object> getNewData() { return newData; }
    public void setNewData(Map<String, Object> newData) { this.newData = newData; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public OffsetDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(OffsetDateTime changedAt) { this.changedAt = changedAt; }
}
