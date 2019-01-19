package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.services.exception.InputValidationException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PointDefServiceTest extends BaseDefServiceTest {

    @Before
    public void beforeEach() throws Exception {
        verifyDefsAreEmpty();
    }

    @Test
    public void testPointAddFailures() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();

        {
            // invalid or insufficient parameters
            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(0L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(-1L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(9999L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(gameId, create("", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(gameId, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(gameId, create("  ", null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(gameId, create("scholar", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(gameId, create("scholar", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addPointDef(gameId, create("scholar", "\t ")))
                    .isInstanceOf(InputValidationException.class);
        }
    }

    @Test
    public void testPointAdds() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int size = getTotalCount(gameId);

        PointDef def = create("reputations", "Total Reputation");
        {
            long defId = addAssert(gameId, def);

            PointDef addedDef = readAssert(defId);
            Assert.assertEquals(defId, addedDef.getId().longValue());
            Assert.assertEquals(def.getName(), addedDef.getName());
            Assert.assertEquals(def.getDisplayName(), addedDef.getDisplayName());

            // one more should be added
            checkTotalCount(gameId, size + 1);
        }

        {
            // add kpi with same name in to the same game should throw an error
            Assertions.assertThatThrownBy(() -> ds.addPointDef(gameId, clone(def)))
                    .isInstanceOf(DbException.class);
        }

        {
            size = getTotalCount(gameId);

            // with description and display name
            PointDef cloned = clone(def);
            cloned.setName("reputation-new");
            cloned.setDisplayName("Total Reputation - Updated");
            cloned.setDescription("Sum of reputation a user has gathered forever.");

            long kpiId = addAssert(gameId, cloned);
            readAssert(kpiId, cloned);

            // one more should be added
            checkTotalCount(gameId, size + 1);
        }

        {
            // add same kpi to a different game must be successful
            GameDef gameNew = createSavedGame("so-updated", "Updated Stackoverflow");
            int sizeNew = getTotalCount(gameNew.getId());

            PointDef clone = clone(def);
            long otherId = addAssert(gameNew.getId(), clone);
            readAssert(otherId, clone);
            checkTotalCount(gameNew.getId(), sizeNew + 1);
        }
    }

    @Test
    public void testPointTypeAdds() throws Exception {
        // @TODO write tests for different types of badges
    }

    @Test
    public void testPointDisable() throws Exception {
        {
            // invalid disable params
            Assertions.assertThatThrownBy(() -> ds.disablePointDef(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disablePointDef(-1L))
                    .isInstanceOf(InputValidationException.class);

            // non existing
            Assert.assertFalse(ds.disablePointDef(9999L));
        }

        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int defSize = getTotalCount(gameId);

        PointDef def1 = create("votes", "Total Votes");
        PointDef def2 = create("stars", "Total Stars");

        PointDef addedDef1 = readAssert(addAssert(gameId, def1), def1);
        PointDef addedDef2 = readAssert(addAssert(gameId, def2), def2);
        checkTotalCount(gameId, defSize + 2);

        {
            // disable def-1
            Assert.assertTrue(ds.disablePointDef(addedDef1.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize + 1);

            // ... but read does
            readAssert(addedDef1.getId());
        }

        {
            // disable def-2
            Assert.assertTrue(ds.disablePointDef(addedDef2.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize);

            // ... but read does
            readAssert(addedDef2.getId());
        }

        {
            // @TODO after disabling, user should be able to add new with a same name again

        }
    }

    private int getTotalCount(long gameId) throws Exception {
        return ds.listPointDefs(gameId).size();
    }

    private void checkTotalCount(long gameId, int expected) throws Exception {
        Assertions.assertThat(getTotalCount(gameId)).isEqualTo(expected);
    }

    private long addAssert(long gameId, PointDef def) throws Exception {
        long l = ds.addPointDef(gameId, def);
        Assert.assertTrue(l > 0);
        return l;
    }

    private PointDef readAssert(long id) throws Exception {
        PointDef def = ds.readPointDef(id);
        Assert.assertNotNull(def);
        Assert.assertEquals(id, def.getId().longValue());
        return def;
    }

    private PointDef readAssert(long id, PointDef check) throws Exception {
        PointDef addedDef = ds.readPointDef(id);
        Assert.assertNotNull(addedDef);
        Assert.assertEquals(id, addedDef.getId().longValue());
        Assert.assertEquals(check.getName(), addedDef.getName());
        Assert.assertEquals(check.getDisplayName(), addedDef.getDisplayName());
        return addedDef;
    }

    private PointDef clone(PointDef other) {
        PointDef def = new PointDef();
        def.setName(other.getName());
        def.setDisplayName(other.getDisplayName());
        def.setDescription(other.getDescription());
        def.setCondition(other.getCondition());
        return def;
    }

    private PointDef create(String name, String displayName) {
        PointDef def = new PointDef();
        def.setName(name);
        def.setDisplayName(displayName);
        return def;
    }

}
