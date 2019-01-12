package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.Utils;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.dto.StatusResponse;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.security.*;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.utils.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IProfileService ps;

    @MockBean
    private OasisConfigurations oasisConfigurations;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private DataCache dataCache;

    @MockBean
    private OasisUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private OasisAuthenticator authenticator;

    private JwtTokenProvider tp;

    @BeforeEach
    void doBefore() throws Exception {
        OasisConfigurations tmpConfigs = new OasisConfigurations();
        tmpConfigs.setAuthJwtSecret("thisissecret");
        tmpConfigs.setAuthJwtExpirationTime(3600L * 1000 * 24);
        tp = new JwtTokenProvider(tmpConfigs);

        BDDMockito.given(oasisConfigurations.getDefaultAdminPassword()).willReturn("admin");
        BDDMockito.given(oasisConfigurations.getDefaultCuratorPassword()).willReturn("curator");
        BDDMockito.given(oasisConfigurations.getDefaultPlayerPassword()).willReturn("player");

        UserProfile isuruUser = createUser(200, "isuru@domain.com", "Isuru Weerarathna");
        UserTeam isuruTeam = new UserTeam(); isuruTeam.setRoleId(UserRole.PLAYER);
        UserPrincipal isuruPrincipal = UserPrincipal.create(isuruUser, isuruTeam);
        UserProfile adminUser = createUser(100, "admin@oasis.com", "Administrator");
        UserTeam adminTeam = new UserTeam(); adminTeam.setRoleId(UserRole.ADMIN);
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser, adminTeam);
        UserProfile curatorUser = createUser(100, "curator@oasis.com", "Handy Curator");
        UserTeam curatorTeam = new UserTeam(); curatorTeam.setRoleId(UserRole.CURATOR);
        UserPrincipal curatorPrincipal = UserPrincipal.create(curatorUser, curatorTeam);
        UserProfile playerUser = createUser(102, "player@oasis.com", "Best Player");
        UserTeam playerTeam = new UserTeam(); playerTeam.setRoleId(UserRole.PLAYER);
        UserPrincipal playerPrincipal = UserPrincipal.create(playerUser, playerTeam);

        Authentication isuruAuth = new TestingAuthenticationToken(isuruPrincipal, "rightpw");
        Authentication adminAuth = new TestingAuthenticationToken(adminPrincipal, "admin");
        Authentication curatorAuth = new TestingAuthenticationToken(curatorPrincipal, "curator");
        Authentication playerAuth = new TestingAuthenticationToken(playerPrincipal, "player");


        BDDMockito.given(ps.readUserProfile("isuru@domain.com")).willReturn(isuruUser);
        BDDMockito.given(ps.readUserProfile("admin@oasis.com")).willReturn(adminUser);
        BDDMockito.given(ps.readUserProfile("curator@oasis.com")).willReturn(curatorUser);
        BDDMockito.given(ps.readUserProfile("player@oasis.com")).willReturn(playerUser);
        BDDMockito.given(ps.findCurrentTeamOfUser(isuruUser.getId())).willReturn(isuruTeam);
        BDDMockito.given(ps.findCurrentTeamOfUser(adminUser.getId())).willReturn(adminTeam);
        BDDMockito.given(ps.findCurrentTeamOfUser(curatorUser.getId())).willReturn(curatorTeam);
        BDDMockito.given(ps.findCurrentTeamOfUser(playerUser.getId())).willReturn(playerTeam);


        BDDMockito.given(dataCache.getAllUserTmpPassword()).willReturn("allpw");

        BDDMockito.given(
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("isuru@domain.com", "rightpw")))
                .willReturn(isuruAuth);
        BDDMockito.given(
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("admin@oasis.com", "admin")))
                .willReturn(adminAuth);
        BDDMockito.given(
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("curator@oasis.com", "curator")))
                .willReturn(curatorAuth);
        BDDMockito.given(
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("player@oasis.com", "player")))
                .willReturn(playerAuth);


        BDDMockito.given(tokenProvider.generateToken(isuruAuth)).willReturn(tp.generateToken(isuruAuth));
        BDDMockito.given(tokenProvider.generateToken(adminAuth)).willReturn(tp.generateToken(adminAuth));
        BDDMockito.given(tokenProvider.generateToken(curatorAuth)).willReturn(tp.generateToken(curatorAuth));
        BDDMockito.given(tokenProvider.generateToken(playerAuth)).willReturn(tp.generateToken(playerAuth));

        BDDMockito.given(userDetailsService.loadUserByUsername(isuruUser.getEmail())).willReturn(isuruPrincipal);
        BDDMockito.given(userDetailsService.loadUserById(isuruUser.getId())).willReturn(isuruPrincipal);
        BDDMockito.given(userDetailsService.loadUserByUsername(adminUser.getEmail())).willReturn(adminPrincipal);
        BDDMockito.given(userDetailsService.loadUserById(adminUser.getId())).willReturn(adminPrincipal);
        BDDMockito.given(userDetailsService.loadUserByUsername(curatorUser.getEmail())).willReturn(curatorPrincipal);
        BDDMockito.given(userDetailsService.loadUserById(curatorUser.getId())).willReturn(curatorPrincipal);
        BDDMockito.given(userDetailsService.loadUserByUsername(playerUser.getEmail())).willReturn(playerPrincipal);
        BDDMockito.given(userDetailsService.loadUserById(playerUser.getId())).willReturn(playerPrincipal);


        BDDMockito.given(authenticator.authenticate("isuru@domain.com", "password"))
                .willReturn(false);
        BDDMockito.given(authenticator.authenticate("isuru@domain.com", "rightpw"))
                .willReturn(true);
    }

    @Test
    void login() throws Exception {
        // without Authorization header, it should fail.
        mvc.perform(postJson("/auth/login", null))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // with corrupted/invalid Authorization header, should fail
        mvc.perform(postJson("/auth/login", ""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mvc.perform(postJson("/auth/login", "Basic abc43243dsads"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mvc.perform(postJson("/auth/login","Basic " + Utils.toBase64("isuru")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        {
            // non reserved user failure for incorrect password
            mvc.perform(postJson("/auth/login", "Basic " + Utils.toBase64("isuru@domain.com:password")))
                    .andExpect(MockMvcResultMatchers.status().is4xxClientError());

            // non reserved user success for correct password
            mvc.perform(postJson("/auth/login",
                    "Basic " + Utils.toBase64("isuru@domain.com:rightpw")))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

            // non reserved user success using global password
            mvc.perform(postJson("/auth/login", "Basic " + Utils.toBase64("isuru@domain.com:allpw")))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        }

        {
            // reserved user fail with incorrect password
            mvc.perform(postJson("/auth/login", "Basic " + Utils.toBase64("admin@oasis.com:wrongpw")))
                    .andExpect(MockMvcResultMatchers.status().is4xxClientError());

            // reserved admin user success with correct password
            mvc.perform(postJson("/auth/login", "Basic " + Utils.toBase64("admin@oasis.com:admin")))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

            // reserved curator user success with correct password
            mvc.perform(postJson("/auth/login", "Basic " + Utils.toBase64("curator@oasis.com:curator")))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

            // reserved player user success with correct password
            mvc.perform(postJson("/auth/login", "Basic " + Utils.toBase64("player@oasis.com:player")))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        }

        // unknown user should fail
        mvc.perform(postJson("/auth/login","Basic " + Utils.toBase64("unknown@domain.com:whoami")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        // user is in ldap but not in the system should fail
        BDDMockito.given(authenticator.authenticate("unknown@domain.com", "whoami"))
            .willReturn(true);
        mvc.perform(postJson("/auth/login","Basic " + Utils.toBase64("unknown@domain.com:whoami")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        // @TODO validate successful login content
    }

    @Test
    void logout() throws Exception {
        BDDMockito.given(ps.logoutUser(Mockito.anyLong(), Mockito.anyLong())).willReturn(true);

        // @TODO only authenticated users should be able to logout

        // already authenticated users can logout
        {
            String playerToken = doLogin("player@oasis.com", "player");
            StatusResponse response = Utils.fromJson(mvc.perform(postJson("/auth/logout", "Bearer " + playerToken))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString(), StatusResponse.class);
            Assertions.assertNotNull(response);
            Assertions.assertTrue(response.isSuccess());
        }

        {
            String playerToken = doLogin("isuru@domain.com", "rightpw");
            StatusResponse response = Utils.fromJson(mvc.perform(postJson("/auth/logout", "Bearer " + playerToken))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString(), StatusResponse.class);
            Assertions.assertNotNull(response);
            Assertions.assertTrue(response.isSuccess());
        }
    }

    private String doLogin(String username, String password) throws Exception {
        String content = mvc.perform(postJson("/auth/login",
                "Basic " + Utils.toBase64(username + ":" + password)))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn()
                .getResponse().getContentAsString();
        Map map = Utils.fromJson(content, Map.class);
        String token = map.get("token").toString();
        BDDMockito.given(tokenProvider.validateToken(token)).willReturn(tp.validateToken(token));
        return token;
    }

    private MockHttpServletRequestBuilder postJson(String url, String authHeader) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON);
        if (authHeader != null) {
            return builder.header("Authorization", authHeader);
        }
        return builder;
    }

    private UserProfile createUser(long id, String email, String name) {
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail(email);
        userProfile.setName(name);
        userProfile.setActivated(true);
        userProfile.setMale(true);
        userProfile.setActive(true);
        userProfile.setId(id);
        return userProfile;
    }

}
