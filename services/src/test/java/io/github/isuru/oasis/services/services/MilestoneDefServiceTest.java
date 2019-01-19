package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.services.exception.InputValidationException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MilestoneDefServiceTest extends BaseDefServiceTest {

    @Before
    public void beforeEach() throws Exception {
        verifyDefsAreEmpty();
    }

    @Test
    public void testMilestoneAddFailures() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();

        {
            // invalid or insufficient parameters
            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(0L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(-1L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(9999L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(gameId, create("", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(gameId, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(gameId, create("  ", null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(gameId, create("scholar", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(gameId, create("scholar", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addMilestoneDef(gameId, create("scholar", "\t ")))
                    .isInstanceOf(InputValidationException.class);
        }
    }

    @Test
    public void testMilestoneAdds() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int size = ds.listMilestoneDefs(gameId).size();

        MilestoneDef def = create("reputations", "Total Reputation");
        {
            long defId = addAssert(gameId, def);

            MilestoneDef addedDef = readAssert(defId);
            Assert.assertEquals(defId, addedDef.getId().longValue());
            Assert.assertEquals(def.getName(), addedDef.getName());
            Assert.assertEquals(def.getDisplayName(), addedDef.getDisplayName());

            // one more should be added
            Assertions.assertThat(ds.listMilestoneDefs(gameId).size()).isEqualTo(size + 1);
        }

        {
            // add kpi with same name in to the same game should throw an error
            Assertions.assertThatThrownBy(() -> ds.addMilestoneDef(gameId, clone(def)))
                    .isInstanceOf(DbException.class);
        }

        {
            size = ds.listMilestoneDefs(gameId).size();

            // with description and display name
            MilestoneDef cloned = clone(def);
            cloned.setName("reputation-new");
            cloned.setDisplayName("Total Reputation - Updated");
            cloned.setDescription("Sum of reputation a user has gathered forever.");

            long kpiId = addAssert(gameId, cloned);
            readAssert(kpiId, cloned);

            // one more should be added
            Assertions.assertThat(ds.listMilestoneDefs(gameId).size()).isEqualTo(size + 1);
        }

        {
            // add same kpi to a different game must be successful
            GameDef gameNew = createSavedGame("so-updated", "Updated Stackoverflow");
            int sizeNew = ds.listMilestoneDefs(gameNew.getId()).size();

            MilestoneDef clone = clone(def);
            long otherId = addAssert(gameNew.getId(), clone);
            readAssert(otherId, clone);
            Assertions.assertThat(ds.listMilestoneDefs(gameNew.getId()).size()).isEqualTo(sizeNew  + 1);
        }
    }

    @Test
    public void testMilestoneTypeAdds() throws Exception {
        // @TODO write tests for different types of badges
    }

    @Test
    public void testMilestoneDisable() throws Exception {
        {
            // invalid disable params
            Assertions.assertThatThrownBy(() -> ds.disableMilestoneDef(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disableMilestoneDef(-1L))
                    .isInstanceOf(InputValidationException.class);

            // non existing
            Assert.assertFalse(ds.disableMilestoneDef(9999L));
        }

        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int kpiSize = ds.listMilestoneDefs(gameId).size();

        MilestoneDef def1 = create("reputation", "Total Reputation");
        MilestoneDef def2 = create("questions", "Total Questions");

        MilestoneDef addedDef1 = readAssert(addAssert(gameId, def1), def1);
        MilestoneDef addedDef2 = readAssert(addAssert(gameId, def2), def2);
        Assert.assertEquals(kpiSize + 2, ds.listMilestoneDefs(gameId).size());

        {
            // disable def-1
            Assert.assertTrue(ds.disableMilestoneDef(addedDef1.getId()));

            // listing should not return disabled ones...
            Assert.assertEquals(kpiSize + 1, ds.listMilestoneDefs(gameId).size());

            // ... but read does
            readAssert(addedDef1.getId());
        }

        {
            // disable def-2
            Assert.assertTrue(ds.disableMilestoneDef(addedDef2.getId()));

            // listing should not return disabled ones...
            Assert.assertEquals(kpiSize, ds.listMilestoneDefs(gameId).size());

            // ... but read does
            readAssert(addedDef2.getId());
        }

        {
            // @TODO after disabling, user should be able to add new with a same name again

        }
    }

    private long addAssert(long gameId, MilestoneDef def) throws Exception {
        long l = ds.addMilestoneDef(gameId, def);
        Assert.assertTrue(l > 0);
        return l;
    }

    private MilestoneDef readAssert(long id) throws Exception {
        MilestoneDef def = ds.readMilestoneDef(id);
        Assert.assertNotNull(def);
        Assert.assertEquals(id, def.getId().longValue());
        return def;
    }

    private MilestoneDef readAssert(long id, MilestoneDef check) throws Exception {
        MilestoneDef addedDef = ds.readMilestoneDef(id);
        Assert.assertNotNull(addedDef);
        Assert.assertEquals(id, addedDef.getId().longValue());
        Assert.assertEquals(check.getName(), addedDef.getName());
        Assert.assertEquals(check.getDisplayName(), addedDef.getDisplayName());
        return addedDef;
    }

    private MilestoneDef clone(MilestoneDef other) {
        MilestoneDef def = new MilestoneDef();
        def.setName(other.getName());
        def.setDisplayName(other.getDisplayName());
        def.setDescription(other.getDescription());
        def.setCondition(other.getCondition());
        return def;
    }

    private MilestoneDef create(String name, String displayName) {
        MilestoneDef def = new MilestoneDef();
        def.setName(name);
        def.setDisplayName(displayName);
        return def;
    }


}
