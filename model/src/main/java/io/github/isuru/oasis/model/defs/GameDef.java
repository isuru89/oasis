package io.github.isuru.oasis.model.defs;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class GameDef {

    private Long id;
    private String name;
    private String displayName;

    private Map<String, Object> constants;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getConstants() {
        return constants;
    }

    public void setConstants(Map<String, Object> constants) {
        this.constants = constants;
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
}
