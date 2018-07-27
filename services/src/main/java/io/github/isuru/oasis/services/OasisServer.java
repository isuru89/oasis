package io.github.isuru.oasis.services;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.BadgesDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.KpisDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.MilestonesDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.defs.PointsDef;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class OasisServer {

    public static void main(String[] args) throws Exception {
//        DbProperties dbProperties = new DbProperties(OasisDbPool.DEFAULT);
//
//        IOasisDao oasisDao = OasisDbFactory.create(dbProperties);
//
//        DefaultOasisApiService apiService = new DefaultOasisApiService(oasisDao);
//
//        FlinkServices flinkServices = new FlinkServices();
//        flinkServices.init();
        putDummyData();

    }

    private static void putDummyData() throws Exception {
        DbProperties dbProperties = new DbProperties(OasisDbPool.DEFAULT);
        dbProperties.setQueryLocation("./scripts/db");
        dbProperties.setUsername("root");
        dbProperties.setPassword("");
        dbProperties.setUrl("jdbc:mariadb://localhost/oasis");

        IOasisDao oasisDao = OasisDbFactory.create(dbProperties);
        IOasisApiService apiService = new DefaultOasisApiService(oasisDao);

        GameDef gameDef = new GameDef();
        gameDef.setName("oasis-test");
        apiService.getGameDefService().createGame(gameDef);

        List<KpiDef> kpiDefs = loadKpis(new File("./scripts/examples/kpis.yml"));
        for (KpiDef kpiDef : kpiDefs) {
            apiService.getGameDefService().addKpiCalculation(kpiDef);
        }
        List<PointDef> pointDefs = loadPoints(new File("./scripts/examples/points.yml"));
        for (PointDef def : pointDefs) {
            apiService.getGameDefService().addPointDef(def);
        }
        List<BadgeDef> badgeDefs = loadBadges(new File("./scripts/examples/badges.yml"));
        for (BadgeDef def : badgeDefs) {
            apiService.getGameDefService().addBadgeDef(def);
        }
        List<MilestoneDef> milestoneDefs = loadMilestones(new File("./scripts/examples/milestones.yml"));
        for (MilestoneDef def : milestoneDefs) {
            apiService.getGameDefService().addMilestoneDef(def);
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
