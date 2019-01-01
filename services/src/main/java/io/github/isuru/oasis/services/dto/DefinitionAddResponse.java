package io.github.isuru.oasis.services.dto;

public class DefinitionAddResponse {

    private String kind;
    private long id;

    public DefinitionAddResponse() {
    }

    public DefinitionAddResponse(String kind, long id) {
        this.kind = kind;
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
