package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.exception.InputValidationException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BadgeDefServiceTest extends BaseDefServiceTest {

    @Before
    public void beforeEach() throws Exception {
        verifyDefsAreEmpty();
    }

    @Test
    public void testBadgeAddFailures() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();

        {
            // invalid or insufficient parameters
            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(0L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(-1L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(9999L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(gameId, create("", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(gameId, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(gameId, create("  ", null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(gameId, create("scholar", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(gameId, create("scholar", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addBadgeDef(gameId, create("scholar", "\t ")))
                    .isInstanceOf(InputValidationException.class);
        }
    }

    @Test
    public void testBadgeAdds() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int size = ds.listBadgeDefs(gameId).size();

        BadgeDef def = create("scholar", "Scholar");
        {
            long defId = addAssert(gameId, def);

            BadgeDef addedDef = readAssert(defId);
            Assert.assertEquals(defId, addedDef.getId().longValue());
            Assert.assertEquals(def.getName(), addedDef.getName());
            Assert.assertEquals(def.getDisplayName(), addedDef.getDisplayName());

            // one more should be added
            Assertions.assertThat(ds.listBadgeDefs(gameId).size()).isEqualTo(size + 1);
        }

        {
            // add kpi with same name in to the same game should throw an error
            Assertions.assertThatThrownBy(() -> ds.addBadgeDef(gameId, clone(def)))
                    .isInstanceOf(DbException.class);
        }

        {
            size = ds.listBadgeDefs(gameId).size();

            // with description and display name
            BadgeDef cloned = clone(def);
            cloned.setName("scholar-new");
            cloned.setDisplayName("Scholar-Updated");
            cloned.setDescription("Normalize favourites before processing.");

            long kpiId = addAssert(gameId, cloned);
            readAssert(kpiId, cloned);

            // one more should be added
            Assertions.assertThat(ds.listBadgeDefs(gameId).size()).isEqualTo(size + 1);
        }

        {
            // add same kpi to a different game must be successful
            GameDef gameNew = createSavedGame("so-updated", "Updated Stackoverflow");
            int sizeNew = ds.listBadgeDefs(gameNew.getId()).size();

            BadgeDef clone = clone(def);
            long otherId = addAssert(gameNew.getId(), clone);
            readAssert(otherId, clone);
            Assertions.assertThat(ds.listBadgeDefs(gameNew.getId()).size()).isEqualTo(sizeNew  + 1);
        }
    }

    @Test
    public void testBadgeTypeAdds() throws Exception {
        // @TODO write tests for different types of badges
    }

    @Test
    public void testBadgeDisable() throws Exception {
        {
            // invalid disable params
            Assertions.assertThatThrownBy(() -> ds.disableBadgeDef(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disableBadgeDef(-1L))
                    .isInstanceOf(InputValidationException.class);

            // non existing
            Assert.assertFalse(ds.disableBadgeDef(9999L));
        }

        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int kpiSize = ds.listBadgeDefs(gameId).size();

        BadgeDef def1 = create("scholar", "Scholar");
        BadgeDef def2 = create("fanatic", "Fanatic");

        BadgeDef addedDef1 = readAssert(addAssert(gameId, def1), def1);
        BadgeDef addedDef2 = readAssert(addAssert(gameId, def2), def2);
        Assert.assertEquals(kpiSize + 2, ds.listBadgeDefs(gameId).size());

        {
            // disable def-1
            Assert.assertTrue(ds.disableBadgeDef(addedDef1.getId()));

            // listing should not return disabled ones...
            Assert.assertEquals(kpiSize + 1, ds.listBadgeDefs(gameId).size());

            // ... but read does
            readAssert(addedDef1.getId());
        }

        {
            // disable def-2
            Assert.assertTrue(ds.disableBadgeDef(addedDef2.getId()));

            // listing should not return disabled ones...
            Assert.assertEquals(kpiSize, ds.listBadgeDefs(gameId).size());

            // ... but read does
            readAssert(addedDef2.getId());
        }

        {
            // after disabling, user should be able to add new with a same name again
            BadgeDef clone = clone(def1);
            readAssert(addAssert(gameId, clone), clone);
        }
    }

    private long addAssert(long gameId, BadgeDef def) throws Exception {
        long l = ds.addBadgeDef(gameId, def);
        Assert.assertTrue(l > 0);
        return l;
    }

    private BadgeDef readAssert(long badgeId) throws Exception {
        BadgeDef def = ds.readBadgeDef(badgeId);
        Assert.assertNotNull(def);
        Assert.assertEquals(badgeId, def.getId().longValue());
        return def;
    }

    private BadgeDef readAssert(long badgeId, BadgeDef check) throws Exception {
        BadgeDef addedDef = ds.readBadgeDef(badgeId);
        Assert.assertNotNull(addedDef);
        Assert.assertEquals(badgeId, addedDef.getId().longValue());
        Assert.assertEquals(check.getName(), addedDef.getName());
        Assert.assertEquals(check.getDisplayName(), addedDef.getDisplayName());
        return addedDef;
    }

    private BadgeDef clone(BadgeDef other) {
        BadgeDef def = new BadgeDef();
        def.setName(other.getName());
        def.setDisplayName(other.getDisplayName());
        def.setDescription(other.getDescription());
        def.setCondition(other.getCondition());
        return def;
    }

    private BadgeDef create(String name, String displayName) {
        BadgeDef def = new BadgeDef();
        def.setName(name);
        def.setDisplayName(displayName);
        return def;
    }

}
