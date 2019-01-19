package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.Maps;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameDefServiceTest extends AbstractServiceTest {

    @Autowired
    private IGameDefService ds;

    @Before
    public void beforeEach() throws Exception {
        resetSchema();

        Assertions.assertThat(ds.listGames()).isEmpty();
        Assertions.assertThat(ds.listBadgeDefs()).isEmpty();
        Assertions.assertThat(ds.listKpiCalculations()).isEmpty();
        Assertions.assertThat(ds.listMilestoneDefs()).isEmpty();
    }

    @Test
    public void testEntityDefValues() {
        GameOptionsDto optionsDto = new GameOptionsDto();
        Assert.assertTrue(optionsDto.isAwardPointsForBadges());
        Assert.assertTrue(optionsDto.isAllowPointCompensation());
        Assert.assertTrue(optionsDto.isAwardPointsForMilestoneCompletion());
        Assert.assertEquals(0.0f, optionsDto.getDefaultBonusPointsForBadge(), 0.01f);
        Assert.assertEquals(0.0f, optionsDto.getDefaultBonusPointsForMilestone(), 0.01f);
    }

    @Test
    public void testCreateGame() throws Exception {
        {
            // invalid game parameters
            Assertions.assertThatThrownBy(() -> ds.createGame(null, new GameOptionsDto()))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.createGame(new GameDef(), null))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.createGame(new GameDef(), new GameOptionsDto()))
                    .isInstanceOf(InputValidationException.class);

            // read non existing game must throw error
            Assertions.assertThatThrownBy(() -> ds.readGame(9876L))
                    .isInstanceOf(InputValidationException.class);
        }

        {
            GameDef gameDef = createGame("Stackoverflow", "StackOverflow Game");
            gameDef.setDescription("Awards reputation and badges based on Q&A on programming questions.");

            long gameId = ds.createGame(gameDef, new GameOptionsDto());
            Assert.assertTrue(gameId > 0);

            GameDef addedGame = ds.readGame(gameId);
            Assert.assertNotNull(addedGame);
            Assert.assertEquals(gameId, addedGame.getId().longValue());
            Assert.assertEquals(gameDef.getName(), addedGame.getName());
            Assert.assertEquals(gameDef.getDisplayName(), addedGame.getDisplayName());
            Assert.assertEquals(gameDef.getDescription(), addedGame.getDescription());
            Assert.assertNull(addedGame.getConstants());

            // for default options
            Assertions.assertThat(ds.listPointDefs(gameId)).hasSize(4);
            Assertions.assertThat(ds.listMilestoneDefs(gameId)).hasSize(0);
            Assertions.assertThat(ds.listKpiCalculations(gameId)).hasSize(0);
            Assertions.assertThat(ds.listBadgeDefs(gameId)).hasSize(0);
            Assertions.assertThat(ds.listLeaderboardDefs(gameId)).hasSize(1);
            Assertions.assertThat(ds.listStatePlays(gameId)).hasSize(0);
            Assertions.assertThat(ds.listChallenges(gameId)).hasSize(0);
        }

        {
            // can't add the game with same name again
            GameDef gameDef = createGame("Stackoverflow", null);

            Assertions.assertThatThrownBy(() -> ds.createGame(gameDef, new GameOptionsDto()))
                    .isInstanceOf(InputValidationException.class);
        }

        {
            // non default options...
            GameDef gameDef = createGame("Bitbucket", "Bitbucket Game");
            gameDef.setDescription("Awards badges and points based on commits.");

            Map<String, Object> consts = new HashMap<>();
            consts.put("MyThreshold", 100);
            gameDef.setConstants(consts);

            GameOptionsDto optionsDto = new GameOptionsDto();
            optionsDto.setAwardPointsForBadges(false);
            optionsDto.setAllowPointCompensation(false);
            optionsDto.setAwardPointsForMilestoneCompletion(false);

            long gameId = ds.createGame(gameDef, optionsDto);
            Assert.assertTrue(gameId > 0);

            GameDef addedGame = ds.readGame(gameId);
            Assert.assertNotNull(addedGame);
            Assert.assertEquals(gameId, addedGame.getId().longValue());
            Assert.assertEquals(gameDef.getName(), addedGame.getName());
            Assert.assertEquals(gameDef.getDisplayName(), addedGame.getDisplayName());
            Assert.assertEquals(gameDef.getDescription(), addedGame.getDescription());
            Assertions.assertThat(addedGame.getConstants())
                    .isNotNull().isNotEmpty()
                    .hasSize(1)
                    .containsEntry("MyThreshold", 100);

            // for default options
            Assertions.assertThat(ds.listPointDefs(gameId)).hasSize(1);
            Assertions.assertThat(ds.listMilestoneDefs(gameId)).hasSize(0);
            Assertions.assertThat(ds.listKpiCalculations(gameId)).hasSize(0);
            Assertions.assertThat(ds.listBadgeDefs(gameId)).hasSize(0);
            Assertions.assertThat(ds.listLeaderboardDefs(gameId)).hasSize(1);
            Assertions.assertThat(ds.listStatePlays(gameId)).hasSize(0);
            Assertions.assertThat(ds.listChallenges(gameId)).hasSize(0);
        }
    }

    @Test
    public void testGameDisable() throws Exception {
        {
            // invalid game parameters
            Assertions.assertThatThrownBy(() -> ds.disableGame(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disableGame(-1L))
                    .isInstanceOf(InputValidationException.class);

            // read non existing game must return unsuccessful
            Assert.assertFalse(ds.disableGame(9876L));
        }

        {
            GameDef game = createGame("Stackoverflow", "Stack Badges Game");
            long gameId = ds.createGame(game, new GameOptionsDto());
            Assert.assertTrue(gameId > 0);

            GameDef addedGame = ds.readGame(gameId);
            Assert.assertNotNull(addedGame);
            Assertions.assertThat(ds.listPointDefs(gameId)).hasSize(4);
            Assertions.assertThat(ds.listLeaderboardDefs(gameId)).hasSize(1);

            Assertions.assertThat(ds.listGames()).hasSize(1);

            // disable game
            Assert.assertTrue(ds.disableGame(gameId));

            // read should return disabled game
            GameDef disabledGame = ds.readGame(gameId);
            Assert.assertNotNull(disabledGame);
            Assert.assertEquals(gameId, disabledGame.getId().longValue());
            Assert.assertEquals(game.getName(), disabledGame.getName());

            // listing should not return disabled game
            Assertions.assertThat(ds.listGames()).isEmpty();
            Assertions.assertThat(ds.listPointDefs(gameId)).isEmpty();
            Assertions.assertThat(ds.listLeaderboardDefs(gameId)).isEmpty();
        }
    }

    @Test
    public void testGameConstantEdit() throws Exception {
        {
            // invalid parameters for add
            Assertions.assertThatThrownBy(() -> ds.addGameConstants(0L, null))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.addGameConstants(-1L, null))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.addGameConstants(1L, null))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.addGameConstants(0L, new HashMap<>()))
                    .isInstanceOf(InputValidationException.class);

            // invalid parameters for remove
            Assertions.assertThatThrownBy(() -> ds.removeGameConstants(0L, null))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.removeGameConstants(-1L, null))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.removeGameConstants(1L, null))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.removeGameConstants(0L, new ArrayList<>()))
                    .isInstanceOf(InputValidationException.class);
        }

        {
            GameDef game = createGame("Stackoverflow", "Stack Badges Game");
            long gameId = ds.createGame(game, new GameOptionsDto());
            Assert.assertTrue(gameId > 0);

            GameDef addedGame = ds.readGame(gameId);
            Assert.assertNotNull(addedGame);
            Assert.assertNull(addedGame.getConstants());

            // add two constants
            Assert.assertTrue(ds.addGameConstants(gameId,
                    Maps.create("DAILY_REP", 200, "TOTAL_QUESTIONS", 5)));
            {
                GameDef modifiedGame = ds.readGame(gameId);
                Assert.assertNotNull(modifiedGame);
                Assert.assertNotNull(modifiedGame.getConstants());
                Assertions.assertThat(modifiedGame.getConstants())
                        .isNotEmpty()
                        .hasSize(2)
                        .containsOnlyKeys("DAILY_REP", "TOTAL_QUESTIONS")
                        .containsEntry("TOTAL_QUESTIONS", 5)
                        .containsEntry("DAILY_REP", 200);
            }

            // add another one (must append)
            Assert.assertTrue(ds.addGameConstants(gameId, Maps.create("MAX_FAVS", 25)));
            {
                GameDef modifiedGame = ds.readGame(gameId);
                Assert.assertNotNull(modifiedGame);
                Assert.assertNotNull(modifiedGame.getConstants());
                Assertions.assertThat(modifiedGame.getConstants())
                        .isNotEmpty()
                        .hasSize(3)
                        .containsOnlyKeys("DAILY_REP", "TOTAL_QUESTIONS", "MAX_FAVS")
                        .containsEntry("TOTAL_QUESTIONS", 5)
                        .containsEntry("MAX_FAVS", 25)
                        .containsEntry("DAILY_REP", 200);

            }

            // remove two from it
            Assert.assertTrue(ds.removeGameConstants(gameId, Arrays.asList("DAILY_REP", "MAX_FAVS")));
            {
                GameDef deletedGame = ds.readGame(gameId);
                Assert.assertNotNull(deletedGame);
                Assert.assertNotNull(deletedGame.getConstants());
                Assertions.assertThat(deletedGame.getConstants())
                        .isNotEmpty()
                        .hasSize(1)
                        .containsOnlyKeys("TOTAL_QUESTIONS")
                        .containsEntry("TOTAL_QUESTIONS", 5);
            }

            // remove non existing constant should still return false (no edit)
            Assert.assertFalse(ds.removeGameConstants(gameId, Collections.singletonList("NONEXIST_CONST")));

            {
                GameDef deletedGame = ds.readGame(gameId);
                Assert.assertNotNull(deletedGame);
                Assert.assertNotNull(deletedGame.getConstants());
                Assertions.assertThat(deletedGame.getConstants())
                        .isNotEmpty()
                        .hasSize(1)
                        .containsOnlyKeys("TOTAL_QUESTIONS")
                        .containsEntry("TOTAL_QUESTIONS", 5);
            }

            // remove the remaining, then constants must be empty
            Assert.assertTrue(ds.removeGameConstants(gameId, Collections.singletonList("TOTAL_QUESTIONS")));

            {
                GameDef allDelGame = ds.readGame(gameId);
                Assert.assertNotNull(allDelGame);
                Assert.assertNotNull(allDelGame.getConstants());
                Assertions.assertThat(allDelGame.getConstants()).isEmpty();
            }

            // try to removing the same key again from all removed constants map, should return false
            Assert.assertFalse(ds.removeGameConstants(gameId, Collections.singletonList("TOTAL_QUESTIONS")));

            {
                GameDef allDelGame = ds.readGame(gameId);
                Assert.assertNotNull(allDelGame);
                Assert.assertNotNull(allDelGame.getConstants());
                Assertions.assertThat(allDelGame.getConstants()).isEmpty();
            }


            // add a new one to empty constant list
            Assert.assertTrue(ds.addGameConstants(gameId, Maps.create("MAX_VOTES", 50)));

            {
                GameDef modifiedGame = ds.readGame(gameId);
                Assert.assertNotNull(modifiedGame);
                Assertions.assertThat(modifiedGame.getConstants())
                        .isNotNull()
                        .isNotEmpty()
                        .hasSize(1)
                        .containsOnlyKeys("MAX_VOTES")
                        .containsEntry("MAX_VOTES", 50);
            }
        }
    }

    private GameDef createGame(String name, String displayName) {
        GameDef gameDef = new GameDef();
        gameDef.setName(name);
        gameDef.setDisplayName(displayName);
        return gameDef;
    }
}
