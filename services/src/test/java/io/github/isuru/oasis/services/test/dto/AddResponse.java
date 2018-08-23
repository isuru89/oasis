package io.github.isuru.oasis.services.test.dto;

/**
 * @author iweerarathna
 */
public class AddResponse {

    private boolean success;
    private Long id;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
