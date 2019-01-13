package io.github.isuru.oasis.services.dto.defs;

import io.github.isuru.oasis.model.defs.GameDef;

/**
 * @author iweerarathna
 */
public class AddGameDto {

    private GameDef def;
    private GameOptionsDto options;

    public AddGameDto() {
    }

    public AddGameDto(GameDef def, GameOptionsDto options) {
        this.def = def;
        this.options = options;
    }

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
