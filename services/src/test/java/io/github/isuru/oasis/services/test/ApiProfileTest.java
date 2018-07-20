package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.model.UserProfile;
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
        user1.setName("Isuru Madushanka");
        user1.setMale(true);
        user1.setEmail("isuru@gmail.com");
        user1.setAvatarId("isuru32.png");
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

        profile.setName("Isuru Weerarathna");
        Assertions.assertTrue(profileService.editUserProfile(profile.getId(), profile));

        UserProfile tmpProfile = profileService.readUserProfileByExtId(10001L);
        Assertions.assertEquals(profile.getName(), tmpProfile.getName());
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        oasisDao.close();
        apiService = null;
    }

}
