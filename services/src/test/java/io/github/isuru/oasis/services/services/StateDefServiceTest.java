package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.RatingDef;
import io.github.isuru.oasis.services.exception.InputValidationException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StateDefServiceTest extends BaseDefServiceTest {

    @Before
    public void beforeEach() throws Exception {
        verifyDefsAreEmpty();
    }

    @Test
    public void testStateAddFailures() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();

        {
            // invalid or insufficient parameters
            Assertions.assertThatThrownBy(
                    () -> ds.addRating(0L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRating(-1L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRating(9999L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addRating(gameId, create("", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRating(gameId, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRating(gameId, create("  ", null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addRating(gameId, create("scholar", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRating(gameId, create("scholar", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRating(gameId, create("scholar", "\t ")))
                    .isInstanceOf(InputValidationException.class);
        }
    }

    @Test
    public void testStateAdds() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int size = getTotalCount(gameId);

        RatingDef def = create("Q&A-Ratio", "Question-Answer Ratio");
        {
            long defId = addAssert(gameId, def);

            RatingDef addedDef = readAssert(defId);
            Assert.assertEquals(defId, addedDef.getId().longValue());
            Assert.assertEquals(def.getName(), addedDef.getName());
            Assert.assertEquals(def.getDisplayName(), addedDef.getDisplayName());

            // one more should be added
            checkTotalCount(gameId, size + 1);
        }

        {
            // add kpi with same name in to the same game should throw an error
            Assertions.assertThatThrownBy(() -> ds.addRating(gameId, clone(def)))
                    .isInstanceOf(DbException.class);
        }

        {
            size = getTotalCount(gameId);

            // with description and display name
            RatingDef cloned = clone(def);
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

            RatingDef clone = clone(def);
            long otherId = addAssert(gameNew.getId(), clone);
            readAssert(otherId, clone);
            checkTotalCount(gameNew.getId(), sizeNew + 1);
        }
    }

    @Test
    public void testStateTypeAdds() throws Exception {
        // @TODO write tests for different types of badges
    }

    @Test
    public void testStateDisable() throws Exception {
        {
            // invalid disable params
            Assertions.assertThatThrownBy(() -> ds.disableRating(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disableRating(-1L))
                    .isInstanceOf(InputValidationException.class);

            // non existing
            Assert.assertFalse(ds.disableRating(9999L));
        }

        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int defSize = getTotalCount(gameId);

        RatingDef def1 = create("votes", "Total Votes");
        RatingDef def2 = create("stars", "Total Stars");

        RatingDef addedDef1 = readAssert(addAssert(gameId, def1), def1);
        RatingDef addedDef2 = readAssert(addAssert(gameId, def2), def2);
        checkTotalCount(gameId, defSize + 2);

        {
            // disable def-1
            Assert.assertTrue(ds.disableRating(addedDef1.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize + 1);

            // ... but read does
            readAssert(addedDef1.getId());
        }

        {
            // disable def-2
            Assert.assertTrue(ds.disableRating(addedDef2.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize);

            // ... but read does
            readAssert(addedDef2.getId());
        }

        {
            // after disabling, user should be able to add new with a same name again
            RatingDef clone = clone(def1);
            readAssert(addAssert(gameId, clone), clone);
        }
    }

    private int getTotalCount(long gameId) throws Exception {
        return ds.listRatings(gameId).size();
    }

    private void checkTotalCount(long gameId, int expected) throws Exception {
        Assertions.assertThat(getTotalCount(gameId)).isEqualTo(expected);
    }

    private long addAssert(long gameId, RatingDef def) throws Exception {
        long l = ds.addRating(gameId, def);
        Assert.assertTrue(l > 0);
        return l;
    }

    private RatingDef readAssert(long id) throws Exception {
        RatingDef def = ds.readRating(id);
        Assert.assertNotNull(def);
        Assert.assertEquals(id, def.getId().longValue());
        return def;
    }

    private RatingDef readAssert(long id, RatingDef check) throws Exception {
        RatingDef addedDef = ds.readRating(id);
        Assert.assertNotNull(addedDef);
        Assert.assertEquals(id, addedDef.getId().longValue());
        Assert.assertEquals(check.getName(), addedDef.getName());
        Assert.assertEquals(check.getDisplayName(), addedDef.getDisplayName());
        return addedDef;
    }

    private RatingDef clone(RatingDef other) {
        RatingDef def = new RatingDef();
        def.setName(other.getName());
        def.setDisplayName(other.getDisplayName());
        def.setDescription(other.getDescription());
        return def;
    }

    private RatingDef create(String name, String displayName) {
        RatingDef def = new RatingDef();
        def.setName(name);
        def.setDisplayName(displayName);
        return def;
    }


}
