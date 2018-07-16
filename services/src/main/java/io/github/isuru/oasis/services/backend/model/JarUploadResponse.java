package io.github.isuru.oasis.services.backend.model;

public class JarUploadResponse {

    private String filename;
    private String status;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
