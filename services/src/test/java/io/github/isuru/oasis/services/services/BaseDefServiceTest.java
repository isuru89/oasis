package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDefServiceTest extends AbstractServiceTest {

    @Autowired
    IGameDefService ds;

    void verifyDefsAreEmpty() throws Exception {
        resetSchema();

        Assertions.assertThat(ds.listGames()).isEmpty();
        Assertions.assertThat(ds.listBadgeDefs()).isEmpty();
        Assertions.assertThat(ds.listKpiCalculations()).isEmpty();
        Assertions.assertThat(ds.listMilestoneDefs()).isEmpty();
    }

    GameDef createGame(String name, String displayName) {
        GameDef gameDef = new GameDef();
        gameDef.setName(name);
        gameDef.setDisplayName(displayName);
        return gameDef;
    }

    GameDef createSavedGame(String name, String displayName) throws Exception {
        GameDef gameDef = new GameDef();
        gameDef.setName(name);
        gameDef.setDisplayName(displayName);

        long game = ds.createGame(gameDef, new GameOptionsDto());
        Assert.assertTrue(game > 0);

        GameDef added = ds.readGame(game);
        Assert.assertNotNull(added);
        Assert.assertEquals(game, added.getId().longValue());
        Assert.assertEquals(gameDef.getName(), added.getName());
        Assert.assertEquals(gameDef.getDisplayName(), added.getDisplayName());
        return added;
    }

}
