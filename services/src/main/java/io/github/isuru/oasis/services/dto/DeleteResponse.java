package io.github.isuru.oasis.services.dto;

public class DeleteResponse {

    private String kind;
    private boolean success;

    public DeleteResponse() {
    }

    public DeleteResponse(String kind, boolean success) {
        this.kind = kind;
        this.success = success;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
