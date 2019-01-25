package io.github.isuru.oasis.game;

import org.junit.jupiter.api.Test;

/**
 * @author iweerarathna
 */
class ChallengeTest extends AbstractTest {

    @Test
    void testChallenges() throws Exception {
        beginTest("challenge-test");
    }

//    private void beginChallenge(String id, ChallengeDef challengeDef) throws Exception {
//        Oasis oasis = new Oasis(id);
//        OasisChallengeExecution execution = new OasisChallengeExecution()
//                .withSource(new CsvReaderPT(id + "/input.csv"))
//                .outputHandler(TestUtils.getAssertConfigs(null, null,
//                        null, new ChallengeSink(id), null))
//                .build(oasis, challengeDef);
//
//        execution.start();
//
//        List<Tuple3<Long, String, Double>> expected = TestUtils.parseWinners(id + "/winners.csv");
//
//        List<ChallengeEvent> challenges = Memo.getChallenges(id);
//        Assertions.assertNotNull(challenges);
//        Assertions.assertEquals(expected.size(), challenges.size());
//
//        assertWinners(expected, challenges);
//    }
//
//    private void assertWinners(List<Tuple3<Long, String, Double>> expected, List<ChallengeEvent> actual) {
//        List<ChallengeEvent> dupActual = new LinkedList<>(actual);
//        for (Tuple3<Long, String, Double> row : expected) {
//
//            boolean foundFlag = false;
//            ChallengeEvent found = null;
//            for (ChallengeEvent given : dupActual) {
//                if (row.f0.equals(given.getUser())
//                        && row.f1.equals(given.getExternalId())
//                        && row.f2.equals(given.getChallengeDef().getPoints())) {
//                    foundFlag = true;
//                    found = given;
//                    break;
//                }
//            }
//
//            if (!foundFlag) {
//                Assertions.fail("Expected point row " + row + " is not found!");
//            } else {
//                dupActual.remove(found);
//                System.out.println("\tFound point: " + row);
//            }
//        }
//    }
//
//    @Test
//    void testOneWinnerChallenge() throws Exception {
//        ChallengeDef def = new ChallengeDef();
//        def.setId(100L);
//        def.setName("challenge-fw");
//        def.setWinnerCount(1);
//        def.setPoints(1000);
//        def.setStartAt(System.currentTimeMillis());
//        def.setExpireAfter(10000);
//        def.setForEvents(new HashSet<>(Arrays.asList("exam")));
//        def.setConditions(Collections.singletonList("marks >= 80"));
//
//        beginChallenge("challenge-fw", def);
//    }
//
//    @Test
//    void testOneWinnerButExpiredChallenge() throws Exception {
//        ChallengeDef def = new ChallengeDef();
//        def.setId(100L);
//        def.setName("challenge-fw-ex");
//        def.setWinnerCount(1);
//        def.setPoints(1000);
//        def.setStartAt(System.currentTimeMillis());
//        def.setExpireAfter(1000);
//        def.setForEvents(new HashSet<>(Arrays.asList("exam")));
//        def.setConditions(Collections.singletonList("marks >= 80"));
//
//        beginChallenge("challenge-fw-ex", def);
//    }
//
//    @Test
//    void testMultiWinnerChallenge() throws Exception {
//        ChallengeDef def = new ChallengeDef();
//        def.setId(101L);
//        def.setName("challenge-mw");
//        def.setWinnerCount(3);
//        def.setPoints(1000);
//        def.setStartAt(System.currentTimeMillis());
//        def.setExpireAfter(10000);
//        def.setForEvents(new HashSet<>(Arrays.asList("exam")));
//        def.setConditions(Collections.singletonList("marks >= 80"));
//
//        beginChallenge("challenge-mw", def);
//    }
//
//    @Test
//    void testMultiWinnerButExpiredChallenge() throws Exception {
//        ChallengeDef def = new ChallengeDef();
//        def.setId(101L);
//        def.setName("challenge-mw-ex");
//        def.setWinnerCount(3);
//        def.setPoints(1000);
//        def.setStartAt(System.currentTimeMillis());
//        def.setExpireAfter(2500);
//        def.setForEvents(new HashSet<>(Arrays.asList("exam")));
//        def.setConditions(Collections.singletonList("marks >= 80"));
//
//        beginChallenge("challenge-mw-ex", def);
//    }

}
