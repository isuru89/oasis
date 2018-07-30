package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author iweerarathna
 */
class ApiProfileTest extends AbstractApiTest {

    private static IOasisDao oasisDao;
    private static IOasisApiService apiService;

    @Test
    void testUsers() throws Exception {
        IProfileService profileService = apiService.getProfileService();

        UserProfile user1 = new UserProfile();
        user1.setName("Arnold Weber");
        user1.setMale(true);
        user1.setEmail("arnold@westworld.com");
        user1.setAvatarId("arnold32.png");
        user1.setExtId(10001L);
        long id = profileService.addUserProfile(user1);

        Assertions.assertTrue(id > 0);
        UserProfile profile = profileService.readUserProfile(id);
        Assertions.assertNotNull(profile);
        Assertions.assertEquals(user1.getName(), profile.getName());
        Assertions.assertEquals(user1.getAvatarId(), profile.getAvatarId());
        Assertions.assertEquals(user1.getEmail(), profile.getEmail());
        Assertions.assertEquals(user1.isMale(), profile.isMale());
        Assertions.assertEquals(user1.getExtId(), profile.getExtId());
        Assertions.assertTrue(profile.isActive());

        profile = profileService.readUserProfileByExtId(10001L);
        Assertions.assertNotNull(profile);
        Assertions.assertEquals(user1.getName(), profile.getName());
        Assertions.assertEquals(user1.getAvatarId(), profile.getAvatarId());
        Assertions.assertEquals(user1.getEmail(), profile.getEmail());
        Assertions.assertEquals(user1.isMale(), profile.isMale());
        Assertions.assertEquals(user1.getExtId(), profile.getExtId());
        Assertions.assertTrue(profile.isActive());

        profile.setName("Bernard Lowe");
        Assertions.assertTrue(profileService.editUserProfile(profile.getId(), profile));

        UserProfile tmpProfile = profileService.readUserProfileByExtId(10001L);
        Assertions.assertEquals(profile.getName(), tmpProfile.getName());

        profile.setAvatarId("bernard.png");
        profile.setMale(false);
        Assertions.assertTrue(profileService.editUserProfile(profile.getId(), profile));

        tmpProfile = profileService.readUserProfileByExtId(10001L);
        Assertions.assertFalse(tmpProfile.isMale());
        Assertions.assertEquals(profile.getAvatarId(), tmpProfile.getAvatarId());

        Assertions.assertTrue(profileService.deleteUserProfile(profile.getId()));
        tmpProfile = profileService.readUserProfile(profile.getId());

        Assertions.assertNotNull(tmpProfile);
        Assertions.assertFalse(tmpProfile.isActive());
    }

    @Test
    void testTeamCrud() throws Exception {
        IProfileService profileService = apiService.getProfileService();

        TeamProfile teamProfile = new TeamProfile();
        teamProfile.setName("QA Team");
        teamProfile.setAvatarId("image_qa.png");
        teamProfile.setTeamScope(2000);
        long tid = profileService.addTeam(teamProfile);
        Assertions.assertTrue(tid > 0);

        TeamProfile profile = profileService.readTeam(tid);
        Assertions.assertNotNull(profile);
        Assertions.assertEquals(teamProfile.getName(), profile.getName());
        Assertions.assertEquals((int)profile.getId(), tid);
        Assertions.assertEquals(teamProfile.getAvatarId(), profile.getAvatarId());
        Assertions.assertEquals(teamProfile.getTeamScope(), profile.getTeamScope());
        Assertions.assertTrue(profile.isActive());
        Assertions.assertNotNull(profile.getCreatedAt());
        Assertions.assertNotNull(profile.getUpdatedAt());

        profile.setAvatarId("new_team_image.png");
        Assertions.assertTrue(profileService.editTeam(profile.getId(), profile));

        teamProfile = profileService.readTeam(profile.getId());
        Assertions.assertEquals(profile.getName(), teamProfile.getName());
        Assertions.assertEquals(profile.getAvatarId(), teamProfile.getAvatarId());
        Assertions.assertEquals(profile.getTeamScope(), teamProfile.getTeamScope());

        profile.setName("QA Team Modified");
        profile.setTeamScope(2001);
        Assertions.assertTrue(profileService.editTeam(profile.getId(), profile));

        teamProfile = profileService.readTeam(profile.getId());
        Assertions.assertEquals(profile.getName(), teamProfile.getName());
        Assertions.assertEquals(profile.getAvatarId(), teamProfile.getAvatarId());
        Assertions.assertEquals(profile.getTeamScope(), teamProfile.getTeamScope());
    }

    @Test
    void testUserTeamAssociation() throws Exception {
        IProfileService profileService = apiService.getProfileService();

        UserProfile user1 = new UserProfile();
        user1.setName("Robert Ford");
        user1.setEmail("ford@westworld.com");
        user1.setMale(true);

        TeamProfile team1 = new TeamProfile();
        team1.setName("leadership");
        TeamProfile team2 = new TeamProfile();
        team2.setName("robot");
        TeamProfile team3 = new TeamProfile();
        team2.setName("qa");

        long u1id = profileService.addUserProfile(user1);
        Assertions.assertTrue(u1id > 0);

        long t1id = profileService.addTeam(team1);
        long t2id = profileService.addTeam(team2);

        Assertions.assertTrue(profileService.addUserToTeam(u1id, t1id));
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(u1id);
        Assertions.assertNotNull(currentTeamOfUser);
        Assertions.assertEquals(t1id, (long) currentTeamOfUser.getTeamId());
        Assertions.assertEquals(u1id, (long) currentTeamOfUser.getUserId());
        Assertions.assertTrue(currentTeamOfUser.getSince() < System.currentTimeMillis());

        // add again to the same team should return false
        Assertions.assertFalse(profileService.addUserToTeam(u1id, t1id));

        // add to robot team
        Assertions.assertTrue(profileService.addUserToTeam(u1id, t2id));
        currentTeamOfUser = profileService.findCurrentTeamOfUser(u1id);
        Assertions.assertNotNull(currentTeamOfUser);
        Assertions.assertEquals(t2id, (long) currentTeamOfUser.getTeamId());
        Assertions.assertEquals(u1id, (long) currentTeamOfUser.getUserId());
        Assertions.assertTrue(currentTeamOfUser.getSince() < System.currentTimeMillis());

    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        DbProperties properties = new DbProperties(OasisDbPool.DEFAULT);
        properties.setUrl("jdbc:mysql://localhost/oasis");
        properties.setUsername("isuru");
        properties.setPassword("isuru");
        File file = new File("./scripts/db");
        if (!file.exists()) {
            file = new File("../scripts/db");
            if (!file.exists()) {
                Assertions.fail("Database scripts directory is not found!");
            }
        }
        properties.setQueryLocation(file.getAbsolutePath());

        oasisDao = OasisDbFactory.create(properties);
        apiService = new DefaultOasisApiService(oasisDao);
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        System.out.println("Shutting down db connection.");
        try {
            oasisDao.executeRawCommand("TRUNCATE OA_USER", null);
            oasisDao.executeRawCommand("TRUNCATE OA_TEAM", null);
            oasisDao.executeRawCommand("TRUNCATE OA_TEAM_USER", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        oasisDao.close();
        apiService = null;
    }

}
