package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.security.JwtAuthenticationEntryPoint;
import io.github.isuru.oasis.services.security.JwtTokenProvider;
import io.github.isuru.oasis.services.security.OasisAuthenticator;
import io.github.isuru.oasis.services.security.OasisUserDetailsService;
import io.github.isuru.oasis.services.services.IProfileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Base64;

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
        UserProfile isuruUser = createUser(200, "isuru@domain.com", "Isuru Weerarathna");
        UserProfile adminUser = createUser(100, "admin@oasis.com", "Administrator");
        UserProfile curatorUser = createUser(100, "curator@oasis.com", "Handy Curator");
        UserProfile playerUser = createUser(102, "player@oasis.com", "Best Player");

        BDDMockito.given(ps.readUserProfile("isuru@domain.com")).willReturn(isuruUser);
        BDDMockito.given(ps.readUserProfile("admin@oasis.com")).willReturn(adminUser);
        BDDMockito.given(ps.readUserProfile("curator@oasis.com")).willReturn(curatorUser);
        BDDMockito.given(ps.readUserProfile("player@oasis.com")).willReturn(playerUser);

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
        mvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization",
                        "Basic " + Base64.getEncoder().encodeToString("isuru@domain.com:rightpw".getBytes())))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        // @TODO non reserved user success using global password

        // @TODO reserved user fail with incorrect password

        // @TODO reserved user success with correct password



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
