package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.services.exception.InputValidationException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KpiDefServiceTest extends BaseDefServiceTest {

    @Before
    public void beforeEach() throws Exception {
        verifyDefsAreEmpty();
    }

    @Test
    public void testKpiAddFailures() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();

        {
            // invalid or insufficient parameters
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(0L, create(null, null, null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(-1L, create(null, null, null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(9999L, create(null, null, null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("", null, null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create(null, null, null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("  ", null, null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("votes", null, "", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("votes", null, null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("votes", null, "\t", null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("stars", null, "views", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("stars", null, "views", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addKpiCalculation(gameId, create("stars", null, "views", " ")))
                    .isInstanceOf(InputValidationException.class);
        }
    }

    @Test
    public void testKpiAdds() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int kpiSize = ds.listKpiCalculations(gameId).size();

        KpiDef kpiDef = create("calc-favs",
                "so-star-event",
                "favourites",
                "fav > 0 ? fav : 0");
        {
            long kpiId = addAssert(gameId, kpiDef);

            KpiDef addedKpi = readAssert(kpiId);
            Assert.assertEquals(kpiId, addedKpi.getId().longValue());
            Assert.assertEquals(kpiDef.getName(), addedKpi.getName());
            Assert.assertEquals(kpiDef.getExpression(), addedKpi.getExpression());
            Assert.assertEquals(kpiDef.getField(), addedKpi.getField());
            Assert.assertEquals(kpiDef.getEvent(), addedKpi.getEvent());
            Assert.assertEquals(kpiDef.getDescription(), addedKpi.getDescription());
            Assert.assertEquals(kpiDef.getDisplayName(), addedKpi.getDisplayName());

            // one more should be added
            Assertions.assertThat(ds.listKpiCalculations(gameId).size()).isEqualTo(kpiSize + 1);
        }

        {
            // add kpi with same name in to the same game should throw an error
            Assertions.assertThatThrownBy(() -> ds.addKpiCalculation(gameId, clone(kpiDef)))
                    .isInstanceOf(DbException.class);
        }

        {
            kpiSize = ds.listKpiCalculations(gameId).size();

            // with description and display name
            KpiDef cloned = clone(kpiDef);
            cloned.setName("calc-favs-new");
            cloned.setDisplayName("Favourite Calculator");
            cloned.setDescription("Normalize favourites before processing.");

            long kpiId = addAssert(gameId, cloned);
            readAssert(kpiId, cloned);

            // one more should be added
            Assertions.assertThat(ds.listKpiCalculations(gameId).size()).isEqualTo(kpiSize + 1);
        }

        {
            // add same kpi to a different game must be successful
            GameDef gameNew = createSavedGame("so-updated", "Updated Stackoverflow");
            int size = ds.listKpiCalculations(gameNew.getId()).size();

            KpiDef clone = clone(kpiDef);
            long otherKpiId = addAssert(gameNew.getId(), clone);
            readAssert(otherKpiId, clone);
            Assertions.assertThat(ds.listKpiCalculations(gameNew.getId()).size()).isEqualTo(size  + 1);
        }
    }

    @Test
    public void testKpiDisable() throws Exception {
        {
            // invalid disable params
            Assertions.assertThatThrownBy(() -> ds.disableKpiCalculation(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disableKpiCalculation(-1L))
                    .isInstanceOf(InputValidationException.class);

            // non existing
            Assert.assertFalse(ds.disableKpiCalculation(9999L));
        }

        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int kpiSize = ds.listKpiCalculations(gameId).size();

        KpiDef kpiDef1 = create("calc-favs",
                "so-star-event",
                "favourites",
                "fav > 0 ? fav : 0");
        KpiDef kpiDef2 = create("calc-reps",
                "so-accept-event",
                "accepts",
                "accepts > 0 ? accepts : 0");
        KpiDef addedDef1 = readAssert(addAssert(gameId, kpiDef1), kpiDef1);
        KpiDef addedDef2 = readAssert(addAssert(gameId, kpiDef2), kpiDef2);
        Assert.assertEquals(kpiSize + 2, ds.listKpiCalculations(gameId).size());

        {
            // disable def-1
            Assert.assertTrue(ds.disableKpiCalculation(addedDef1.getId()));

            // listing should not return disabled ones...
            Assert.assertEquals(kpiSize + 1, ds.listKpiCalculations(gameId).size());

            // ... but read does
            readAssert(addedDef1.getId());
        }

        {
            // disable def-2
            Assert.assertTrue(ds.disableKpiCalculation(addedDef2.getId()));

            // listing should not return disabled ones...
            Assert.assertEquals(kpiSize, ds.listKpiCalculations(gameId).size());

            // ... but read does
            readAssert(addedDef2.getId());
        }

        {
            // @TODO after disabling, user should be able to add new with a same name again

        }
    }

    private long addAssert(long gameId, KpiDef def) throws Exception {
        long l = ds.addKpiCalculation(gameId, def);
        Assert.assertTrue(l > 0);
        return l;
    }

    private KpiDef readAssert(long kpiId) throws Exception {
        KpiDef addedKpi = ds.readKpiCalculation(kpiId);
        Assert.assertNotNull(addedKpi);
        Assert.assertEquals(kpiId, addedKpi.getId().longValue());
        return addedKpi;
    }

    private KpiDef readAssert(long kpiId, KpiDef check) throws Exception {
        KpiDef addedKpi = ds.readKpiCalculation(kpiId);
        Assert.assertNotNull(addedKpi);
        Assert.assertEquals(kpiId, addedKpi.getId().longValue());
        Assert.assertEquals(check.getName(), addedKpi.getName());
        Assert.assertEquals(check.getExpression(), addedKpi.getExpression());
        Assert.assertEquals(check.getField(), addedKpi.getField());
        Assert.assertEquals(check.getEvent(), addedKpi.getEvent());
        Assert.assertEquals(check.getDescription(), addedKpi.getDescription());
        Assert.assertEquals(check.getDisplayName(), addedKpi.getDisplayName());
        return addedKpi;
    }

    private KpiDef clone(KpiDef other) {
        KpiDef kpiDef = new KpiDef();
        kpiDef.setExpression(other.getExpression());
        kpiDef.setField(other.getField());
        kpiDef.setEvent(other.getEvent());
        kpiDef.setName(other.getName());
        kpiDef.setDisplayName(other.getDisplayName());
        kpiDef.setDescription(other.getDescription());
        return kpiDef;
    }

    private KpiDef create(String name, String event, String field, String expression) {
        KpiDef kpiDef = new KpiDef();
        kpiDef.setName(name);
        kpiDef.setEvent(event);
        kpiDef.setField(field);
        kpiDef.setExpression(expression);
        return kpiDef;
    }
}
