package io.github.isuru.oasis.services.utils;

import java.sql.Blob;
import java.sql.Timestamp;

/**
 * @author iweerarathna
 */
public class EventSourceToken {

    public static final String INTERNAL_NAME = "internal";

    private Integer id;
    private String sourceName;
    private String token;
    private Blob secretKey;
    private Blob publicKey;
    private String displayName;
    private boolean downloaded;
    private boolean internal;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Blob getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Blob publicKey) {
        this.publicKey = publicKey;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Blob getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(Blob secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}
