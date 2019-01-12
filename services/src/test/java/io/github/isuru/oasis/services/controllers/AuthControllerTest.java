package io.github.isuru.oasis.services.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.security.*;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.utils.UserRole;
import org.junit.Test;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Base64;
import java.util.Map;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

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

    @Test
    public void login() throws Exception {
        OasisConfigurations oasisConfigurations = new OasisConfigurations();
        oasisConfigurations.setAuthJwtSecret("thisissecret");
        oasisConfigurations.setAuthJwtExpirationTime(3600L * 1000 * 24);
        JwtTokenProvider tp = new JwtTokenProvider(oasisConfigurations);

        UserProfile isuruUser = createUser(200, "isuru@domain.com", "Isuru Weerarathna");
        UserTeam isuruTeam = new UserTeam(); isuruTeam.setRoleId(UserRole.PLAYER);
        UserProfile adminUser = createUser(100, "admin@oasis.com", "Administrator");
        UserTeam adminTeam = new UserTeam(); adminTeam.setRoleId(UserRole.ADMIN);
        UserProfile curatorUser = createUser(100, "curator@oasis.com", "Handy Curator");
        UserTeam curatorTeam = new UserTeam(); curatorTeam.setRoleId(UserRole.CURATOR);
        UserProfile playerUser = createUser(102, "player@oasis.com", "Best Player");
        UserTeam playerTeam = new UserTeam(); playerTeam.setRoleId(UserRole.PLAYER);

        Authentication isuruAuth = new TestingAuthenticationToken(UserPrincipal.create(isuruUser, isuruTeam), "rightpw");
        Authentication adminAuth = new TestingAuthenticationToken(UserPrincipal.create(adminUser, adminTeam), "admin");
        Authentication curatorAuth = new TestingAuthenticationToken(UserPrincipal.create(curatorUser, curatorTeam), "curator");
        Authentication playerAuth = new TestingAuthenticationToken(UserPrincipal.create(playerUser, playerTeam), "player");

        BDDMockito.given(ps.readUserProfile("isuru@domain.com")).willReturn(isuruUser);
        BDDMockito.given(ps.readUserProfile("admin@oasis.com")).willReturn(adminUser);
        BDDMockito.given(ps.readUserProfile("curator@oasis.com")).willReturn(curatorUser);
        BDDMockito.given(ps.readUserProfile("player@oasis.com")).willReturn(playerUser);

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

        BDDMockito.given(ps.logoutUser(Mockito.anyLong(), Mockito.anyLong())).willReturn(true);

        // without Authorization header, it should fail.
        mvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // @TODO with corrupted/invalid Authorization header, should fail


        BDDMockito.given(authenticator.authenticate("isuru@domain.com", "password"))
                .willReturn(false);
        BDDMockito.given(authenticator.authenticate("isuru@domain.com", "rightpw"))
                .willReturn(true);

        // non reserved user failure for incorrect password
        mvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization",
                        "Basic " + Base64.getEncoder().encodeToString("isuru@domain.com:password".getBytes())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        // non reserved user success for correct password
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization",
                        "Basic " + Base64.getEncoder().encodeToString("isuru@domain.com:rightpw".getBytes())))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
        Map map = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
//        String token = map.get("token");
        System.out.println(map.get("token"));

        // @TODO non reserved user success using global password

        // @TODO reserved user fail with incorrect password

        // @TODO reserved user success with correct password



    }

    @Test
    public void logout() throws Exception {

    }

    private String doLogin() {
        return null;
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
