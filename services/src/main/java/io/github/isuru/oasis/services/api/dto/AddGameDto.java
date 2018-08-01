package io.github.isuru.oasis.services.api.dto;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.model.GameOptionsDto;

/**
 * @author iweerarathna
 */
public class AddGameDto {

    private GameDef def;
    private GameOptionsDto options;

    public GameDef getDef() {
        return def;
    }

    public void setDef(GameDef def) {
        this.def = def;
    }

    public GameOptionsDto getOptions() {
        return options;
    }

    public void setOptions(GameOptionsDto options) {
        this.options = options;
    }
}
