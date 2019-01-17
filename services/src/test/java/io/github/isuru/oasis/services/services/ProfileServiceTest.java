package io.github.isuru.oasis.services.services;

import com.github.slugify.Slugify;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class ProfileServiceTest extends AbstractServiceTest {

    private static final Slugify SLUGIFY = new Slugify();

    @Autowired
    private IProfileService ps;

    @Before
    public void verify() throws Exception {
        // drop schema
        resetSchema();

        List<TeamScope> teamScopes = ps.listTeamScopes();
        Assertions.assertThat(teamScopes)
                .isNotNull()
                .hasSize(1);

        Integer scopeId = teamScopes.get(0).getId();
        List<TeamProfile> teamProfiles = ps.listTeams(scopeId);
        Assertions.assertThat(teamProfiles)
                .isNotNull()
                .hasSize(1);

        Integer teamId = teamProfiles.get(0).getId();
        List<UserProfile> userProfiles = ps.listUsers(teamId, 0, 100);
        Assertions.assertThat(userProfiles)
                .isNotNull()
                .hasSize(4);        // (admin, curator, player) + default

        Assertions.assertThat(userProfiles)
                .extracting("email")
                .contains("admin@oasis.com",
                        "curator@oasis.com",
                        "player@oasis.com",
                        "default@default.oasis.com");
    }

    @Test
    public void testAddUserProfileFailures() {
        {
            // add without email should fail
            Assertions.assertThatThrownBy(
                    () -> ps.addUserProfile(createProfile("test add user", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ps.addUserProfile(createProfile(null, "isuru@domain.com")))
                    .isInstanceOf(InputValidationException.class);

            // no reserved users should be able to add again
            Assertions.assertThatThrownBy(
                    () -> ps.addUserProfile(createProfile("Admin", "admin@oasis.com")))
                    .isInstanceOf(DbException.class);
            Assertions.assertThatThrownBy(
                    () -> ps.addUserProfile(createProfile("Curator", "curator@oasis.com")))
                    .isInstanceOf(DbException.class);
            Assertions.assertThatThrownBy(
                    () -> ps.addUserProfile(createProfile("Player", "player@oasis.com")))
                    .isInstanceOf(DbException.class);
            Assertions.assertThatThrownBy(
                    () -> ps.addUserProfile(createProfile("Default", "default@default.oasis.com")))
                    .isInstanceOf(DbException.class);
        }
    }

    @Test
    public void testAddUserProfile() throws Exception {
        {
            UserProfile profile = createProfile("Isuru Weerarathna", "isuru@dmain.com");
            profile.setMale(true);
            profile.setActivated(true);
            profile.setExtId(1001L);
            profile.setNickName("isuru");

            long id = ps.addUserProfile(profile);
            Assertions.assertThat(id).isGreaterThan(0);

            UserProfile ap = ps.readUserProfile(id);
            Assertions.assertThat(ap).isNotNull();
            SoftAssertions asserts = new SoftAssertions();
            asserts.assertThat(ap.getId()).isEqualTo(id);
            asserts.assertThat(ap.getEmail()).isEqualTo(profile.getEmail());
            asserts.assertThat(ap.getName()).isEqualTo(profile.getName());
            asserts.assertThat(ap.getExtId()).isEqualTo(profile.getExtId());
            asserts.assertThat(ap.getNickName()).isEqualTo(profile.getNickName());
            asserts.assertThat(ap.isActive()).isTrue();
            asserts.assertThat(ap.isActivated()).isTrue();
            asserts.assertThat(ap.getLastLogoutAt()).isNull();
            asserts.assertThat(ap.getPassword()).isNull();
            asserts.assertThat(ap.getAvatarId()).isNull();
            asserts.assertThat(ap.getHeroId()).isNull();
            asserts.assertThat(ap.getHeroLastUpdatedAt()).isNull();
            asserts.assertThat(ap.getHeroUpdateTimes()).isEqualTo(0);
            asserts.assertAll();

            // should not be able to add the same user again.
            UserProfile dupProfile = createProfile("Isuru Madushanka", profile.getEmail());
            profile.setMale(true);
            profile.setActivated(true);
            profile.setExtId(1001L);
            profile.setNickName("isuru");

            Assertions.assertThatThrownBy(() -> ps.addUserProfile(dupProfile))
                    .isInstanceOf(DbException.class);
        }
    }

    @Test
    public void testAddTeamScope() throws Exception {
        {
            TeamScope scope = createScope("HR", "Human Resource", 123L);

            long sid = ps.addTeamScope(scope);
            Assertions.assertThat(sid).isGreaterThan(0);
            TeamScope ascp = ps.readTeamScope(sid);
            Assertions.assertThat(ascp).isNotNull();
            Assertions.assertThat(ascp.getName()).isEqualTo(scope.getName());
            Assertions.assertThat(ascp.getDisplayName()).isEqualTo(scope.getDisplayName());
            Assertions.assertThat(ascp.getExtId()).isEqualTo(scope.getExtId());
            Assertions.assertThat(ascp.isActive()).isTrue();
            Assertions.assertThat(ascp.getCreatedAt()).isBefore(new Date());
            Assertions.assertThat(ascp.getUpdatedAt()).isBefore(new Date());
            Assertions.assertThat(ascp.getUpdatedAt()).isEqualTo(ascp.getCreatedAt());

            // check default team exist
            List<TeamProfile> teamProfiles = ps.listTeams(sid);
            Assertions.assertThat(teamProfiles)
                    .hasSize(1)
                    .extracting("name")
                    .contains("default_" + SLUGIFY.slugify(scope.getName()));

            // check default user exists
            List<UserProfile> userProfiles = ps.listUsers(teamProfiles.get(0).getId(), 0, 10);
            Assertions.assertThat(userProfiles)
                    .hasSize(1)
                    .extracting("email")
                    .contains(String.format("default@%s.oasis.com", SLUGIFY.slugify(scope.getName())));


            {
                TeamScope scp1 = ps.readTeamScope(sid);
                TeamScope scp2 = ps.readTeamScope(scope.getName());
                Assertions.assertThat(scp1).isNotNull()
                    .hasFieldOrPropertyWithValue("name", scope.getName())
                    .hasFieldOrPropertyWithValue("displayName", scope.getDisplayName())
                    .hasFieldOrPropertyWithValue("extId", scope.getExtId());
                Assertions.assertThat(scp2).isNotNull()
                    .hasFieldOrPropertyWithValue("name", scope.getName())
                    .hasFieldOrPropertyWithValue("displayName", scope.getDisplayName())
                    .hasFieldOrPropertyWithValue("extId", scope.getExtId());
                Assertions.assertThat(scp1).isEqualTo(scp2);
            }
        }

        {
            // cannot add the same scope with name again
            TeamScope scope = createScope("HR", "Human Resource", 123L);

            Assertions.assertThatThrownBy(() -> ps.addTeamScope(scope))
                    .isInstanceOf(DbException.class);
        }

        {
            List<TeamScope> teamScopes = ps.listTeamScopes();
            Assertions.assertThat(teamScopes).isNotNull().hasSize(2);
        }
    }

    @Test
    public void testAddTeam() throws Exception {
        TeamScope scopeHr = createScope("HR", "Human Resource", 120L);
        long hrId = ps.addTeamScope(scopeHr);
        TeamScope scopeFin = createScope("Finance", "Finance Dept.", 121L);
        long finId = ps.addTeamScope(scopeFin);

        {
            // without scope id, should fail
            TeamProfile team = createTeam(null, "Team 1");
            Assertions.assertThatThrownBy(() -> ps.addTeam(team))
                    .isInstanceOf(InputValidationException.class);
        }

        {
            // add first team
            TeamProfile hrTeam1 = createTeam(hrId, "HR - Team Recruiters");
            long tid = ps.addTeam(hrTeam1);
            Assertions.assertThat(tid).isGreaterThan(0);

            {
                List<UserProfile> userProfiles = ps.listUsers(tid, 0, 10);
                Assertions.assertThat(userProfiles).isNotNull()
                        .hasSize(1)
                        .extracting("email")
                        .contains(String.format("user@%s.oasis.com", SLUGIFY.slugify(hrTeam1.getName())));
            }

            {
                TeamProfile tp = ps.readTeam(tid);
                Assertions.assertThat(tp.getName()).isEqualTo(hrTeam1.getName());
                Assertions.assertThat(tp.getTeamScope()).isEqualTo(hrId);
                Assertions.assertThat(tp.getId()).isGreaterThan(0);
                Assertions.assertThat(tp.getAvatarId()).isNull();
                Assertions.assertThat(tp.getCreatedAt()).isBefore(new Date());
                Assertions.assertThat(tp.getUpdatedAt()).isBefore(new Date());
                Assertions.assertThat(tp.getUpdatedAt()).isEqualTo(tp.getCreatedAt());
            }

            // add other team
            TeamProfile finTeam1 = createTeam(finId, "Team Insurance");
            long fid = ps.addTeam(finTeam1);
            Assertions.assertThat(fid).isGreaterThan(0);

            {
                List<UserProfile> userProfiles = ps.listUsers(fid, 0, 10);
                Assertions.assertThat(userProfiles).isNotNull()
                        .hasSize(1)
                        .extracting("email")
                        .contains(String.format("user@%s.oasis.com", SLUGIFY.slugify(hrTeam1.getName())));
            }

            {
                TeamProfile tp = ps.readTeam(fid);
                Assertions.assertThat(tp.getName()).isEqualTo(finTeam1.getName());
                Assertions.assertThat(tp.getTeamScope()).isEqualTo(finId);
                Assertions.assertThat(tp.getId()).isGreaterThan(0);
                Assertions.assertThat(tp.getAvatarId()).isNull();
                Assertions.assertThat(tp.getCreatedAt()).isBefore(new Date());
                Assertions.assertThat(tp.getUpdatedAt()).isBefore(new Date());
                Assertions.assertThat(tp.getUpdatedAt()).isEqualTo(tp.getCreatedAt());
            }

            // now there must have one team in each scope
            Assertions.assertThat(ps.listTeams(hrId)).isNotNull().hasSize(1);
            Assertions.assertThat(ps.listTeams(finId)).isNotNull().hasSize(1);
        }
    }

    private TeamProfile createTeam(Number scopeId, String name) {
        TeamProfile team = new TeamProfile();
        if (scopeId != null) {
            team.setTeamScope(scopeId.intValue());
        }
        team.setName(name);
        return team;
    }

    private TeamScope createScope(String name, String displayName, Long extId) {
        TeamScope scope = new TeamScope();
        scope.setDisplayName(displayName);
        scope.setName(name);
        scope.setExtId(extId);
        return scope;
    }

    private UserProfile createProfile(String username, String email) {
        UserProfile profile = new UserProfile();
        profile.setName(username);
        profile.setEmail(email);
        return profile;
    }

}
