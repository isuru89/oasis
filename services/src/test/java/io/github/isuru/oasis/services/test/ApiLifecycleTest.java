package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.BadgesDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.KpisDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.MilestonesDef;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.defs.PointsDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.services.LifeCycleServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
class ApiLifecycleTest extends AbstractApiTest {

    @Test
    void testRuleWrite() throws Exception {
        GameDef gameDef = new GameDef();
        gameDef.setName("oasis-test");
        Long gameId = apiService.getGameDefService().createGame(gameDef, new GameOptionsDto());

        List<KpiDef> kpiDefs = loadKpis(new File("../scripts/examples/kpis.yml"));
        for (KpiDef kpiDef : kpiDefs) {
            apiService.getGameDefService().addKpiCalculation(gameId, kpiDef);
        }
        List<PointDef> pointDefs = loadPoints(new File("../scripts/examples/points.yml"));
        for (PointDef def : pointDefs) {
            apiService.getGameDefService().addPointDef(gameId, def);
        }
        List<BadgeDef> badgeDefs = loadBadges(new File("../scripts/examples/badges.yml"));
        for (BadgeDef def : badgeDefs) {
            apiService.getGameDefService().addBadgeDef(gameId, def);
        }
        List<MilestoneDef> milestoneDefs = loadMilestones(new File("../scripts/examples/milestones.yml"));
        for (MilestoneDef def : milestoneDefs) {
            apiService.getGameDefService().addMilestoneDef(gameId, def);
        }

        LifeCycleServiceImpl lifecycleService = (LifeCycleServiceImpl) apiService.getLifecycleService();
        StringWriter writer = new StringWriter();
        lifecycleService.writeGameRulesFile(gameId, true, writer);
        writer.flush();

        String txt = writer.getBuffer().toString();
        FileUtils.write(new File("../scripts/examples/oasis.yml"), txt, StandardCharsets.UTF_8);
        //System.out.println(txt);

        Yaml yaml = new Yaml();
        OasisGameDef oasisGameDef = yaml.loadAs(txt, OasisGameDef.class);
        System.out.println(oasisGameDef);

        Assertions.assertEquals(kpiDefs.size(), oasisGameDef.getKpis().size());
        Assertions.assertEquals(pointDefs.size() + 3, oasisGameDef.getPoints().size());
        Assertions.assertEquals(badgeDefs.size(), oasisGameDef.getBadges().size());
        Assertions.assertEquals(milestoneDefs.size(), oasisGameDef.getMilestones().size());
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

            return milestonesDef.getMilestones().stream()
                    .peek(p -> p.setDisplayName(p.getName()))
                    .collect(Collectors.toList());
        }
    }

    private static List<PointDef> loadPoints(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            PointsDef pointsDef = yaml.loadAs(inputStream, PointsDef.class);

            return pointsDef.getPoints().stream().peek(p -> p.setDisplayName(p.getName())).collect(Collectors.toList());
        }
    }

    private static List<BadgeDef> loadBadges(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            BadgesDef badgesDef = yaml.loadAs(inputStream, BadgesDef.class);

            return badgesDef.getBadges().stream()
                    .peek(p -> p.setDisplayName(p.getName()))
                    .collect(Collectors.toList());
        }
    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        dbStart();
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        dbClose("OA_DEFINITION");
    }

}
