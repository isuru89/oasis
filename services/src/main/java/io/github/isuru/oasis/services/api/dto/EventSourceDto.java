package io.github.isuru.oasis.services.api.dto;

import io.github.isuru.oasis.services.utils.EventSourceToken;

public class EventSourceDto {

    private Integer id;
    private String sourceName;
    private String displayName;
    private boolean downloaded;
    private boolean active;
    private long createdAt;

    private EventSourceDto() {
    }

    public static EventSourceDto from(EventSourceToken token) {
        EventSourceDto dto = new EventSourceDto();
        dto.setId(token.getId());
        dto.setActive(token.isActive());
        dto.setCreatedAt(token.getCreatedAt().getTime());
        dto.setDisplayName(token.getDisplayName());
        dto.setDownloaded(token.isDownloaded());
        dto.setSourceName(token.getSourceName());
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
