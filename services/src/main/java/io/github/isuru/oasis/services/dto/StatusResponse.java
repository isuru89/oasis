package io.github.isuru.oasis.services.dto;

public class StatusResponse {

    private boolean success;

    public StatusResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
