package io.github.isuru.oasis.services.dto.crud;

public class TeamScopeAddDto {

    private String name;
    private String displayName;
    private Long extId;
    private boolean autoScope;

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

    public boolean isAutoScope() {
        return autoScope;
    }

    public void setAutoScope(boolean autoScope) {
        this.autoScope = autoScope;
    }
}
