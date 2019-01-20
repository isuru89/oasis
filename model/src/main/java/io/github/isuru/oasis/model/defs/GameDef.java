package io.github.isuru.oasis.model.defs;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class GameDef extends BaseDef {

    private Map<String, Object> constants;

    public Map<String, Object> getConstants() {
        return constants;
    }

    public void setConstants(Map<String, Object> constants) {
        this.constants = constants;
    }

}
