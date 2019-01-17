package io.github.isuru.oasis.services.model;

import java.sql.Timestamp;

/**
 * @author iweerarathna
 */
public class TeamScope {

    private Integer id;
    private String name;
    private String displayName;
    private Long extId;
    private boolean autoScope;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public boolean isAutoScope() {
        return autoScope;
    }

    public void setAutoScope(boolean autoScope) {
        this.autoScope = autoScope;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getExtId() {
        return extId;
    }

    public void setExtId(Long extId) {
        this.extId = extId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamScope scope = (TeamScope) o;

        return id != null ? id.equals(scope.id) : scope.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
