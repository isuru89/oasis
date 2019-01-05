package io.github.isuru.oasis.services;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.model.db.OasisDbPool;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.BadgesDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.KpisDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.MilestonesDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.defs.PointsDef;
import io.github.isuru.oasis.services.services.IOasisApiService;
import io.github.isuru.oasis.services.services.DefaultOasisApiService;
import io.github.isuru.oasis.services.model.GameOptionsDto;
import io.github.isuru.oasis.services.utils.OasisOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author iweerarathna
 */
class DummyRunner {

    public static void main(String[] args) throws Exception {
        putDummyData();
    }

    private static void putDummyData() throws Exception {
        DbProperties dbProperties = new DbProperties(OasisDbPool.DEFAULT);
        dbProperties.setQueryLocation("./scripts/db");
        dbProperties.setUsername("isuru");
        dbProperties.setPassword("isuru");
        dbProperties.setUrl("jdbc:mysql://localhost/oasis");

        IOasisDao oasisDao = OasisDbFactory.create(dbProperties);
        OasisOptions oasisOptions = new OasisOptions();
        IOasisApiService apiService = new DefaultOasisApiService(oasisDao, oasisOptions, Configs.create());

        GameDef gameDef = new GameDef();
        gameDef.setName("oasis-test");
        Long gameId = apiService.getGameDefService().createGame(gameDef, new GameOptionsDto());

        List<KpiDef> kpiDefs = loadKpis(new File("./scripts/examples/kpis.yml"));
        for (KpiDef kpiDef : kpiDefs) {
            apiService.getGameDefService().addKpiCalculation(gameId, kpiDef);
        }
        List<PointDef> pointDefs = loadPoints(new File("./scripts/examples/points.yml"));
        for (PointDef def : pointDefs) {
            apiService.getGameDefService().addPointDef(gameId, def);
        }
        List<BadgeDef> badgeDefs = loadBadges(new File("./scripts/examples/badges.yml"));
        for (BadgeDef def : badgeDefs) {
            apiService.getGameDefService().addBadgeDef(gameId, def);
        }
        List<MilestoneDef> milestoneDefs = loadMilestones(new File("./scripts/examples/milestones.yml"));
        for (MilestoneDef def : milestoneDefs) {
            apiService.getGameDefService().addMilestoneDef(gameId, def);
        }
    }

    private static List<KpiDef> loadKpis(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            KpisDef kpisDef = yaml.loadAs(inputStream, KpisDef.class);

            return kpisDef.getCalculations();
        }
    }

    private static List<MilestoneDef> loadMilestones(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            MilestonesDef milestonesDef = yaml.loadAs(inputStream, MilestonesDef.class);

            return milestonesDef.getMilestones();
        }
    }

    private static List<PointDef> loadPoints(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            PointsDef pointsDef = yaml.loadAs(inputStream, PointsDef.class);

            return pointsDef.getPoints();
        }
    }

    private static List<BadgeDef> loadBadges(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            BadgesDef badgesDef = yaml.loadAs(inputStream, BadgesDef.class);

            return badgesDef.getBadges();
        }
    }


}
