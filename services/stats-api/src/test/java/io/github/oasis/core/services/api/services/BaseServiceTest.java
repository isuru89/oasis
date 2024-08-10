package io.github.oasis.core.services.api.services;

import io.github.oasis.core.Game;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.model.GameState;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.ApiConstants;
import io.github.oasis.core.services.KeyGeneratorSupport;
import io.github.oasis.core.services.api.app.request.EventSourceCreateRequest;
import io.github.oasis.core.services.api.beans.TestEngineConfigs;
import io.github.oasis.core.services.api.infrastructure.beans.jpa.JpaBasedDao;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.EventSourceRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.GameRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.PlayerRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.AssignmentRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.TeamRepository;
import io.github.oasis.core.services.api.infrastructure.repository.dto.GameDto;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestEngineConfigs.class)
public class BaseServiceTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EventSourceRepository eventSourceRepository;


    @Autowired
    private JpaBasedDao dao;

    @Autowired
    private KeyGeneratorSupport keyGenerator;

    protected String baseUrl;

    @BeforeEach
    public void beforeEach() {
        baseUrl = "http://localhost:" + port;

        gameRepository.deleteAll();
        eventSourceRepository.deleteAll();

        assignmentRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        gameRepository.deleteAll();
    }

    HttpEntity<?> withAdminReq() {
        var headers = new HttpHeaders();
        headers.add(ApiConstants.APP_ID_HEADER, "root");
        headers.add(ApiConstants.APP_KEY_HEADER, "root");

        return new HttpEntity<>(headers);
    }

    <T> HttpEntity<T> withAdminReq(T body) {
        var headers = new HttpHeaders();
        headers.add(ApiConstants.APP_ID_HEADER, "root");
        headers.add(ApiConstants.APP_KEY_HEADER, "root");

        return new HttpEntity<>(body, headers);
    }

    @Nonnull
    <R> R mustPostSuccess(String url, HttpEntity<?> req, Class<R> responseClz) {
        var response = restTemplate.postForEntity(url, req, responseClz);
        System.out.println(response.getStatusCode().value());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful(), "expected ");
        Assertions.assertNotNull(response.getBody());

        return response.getBody();
    }

    @Nonnull
    <R> R mustGetSuccess(String url, HttpEntity<?> req, Class<R> responseClz) {
        var response = restTemplate.exchange(url, HttpMethod.GET, req, responseClz);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());

        return response.getBody();
    }

    void mustDeleteSuccess(String url, HttpEntity<?> req) {
        var response = restTemplate.exchange(url, HttpMethod.DELETE, req, Void.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testEventSourceCreation() {
        var request = new EventSourceCreateRequest("test-eventsource");

        {
            var response = mustPostSuccess(baseUrl + "/api/admin/event-sources", withAdminReq(request), EventSource.class);
            System.out.println(response.toString());
        }

        {
            var response = mustGetSuccess(baseUrl + "/api/admin/event-sources", withAdminReq(), EventSource[].class);
            for (var es : response) {
                System.out.println(es.toString());
                mustDeleteSuccess(baseUrl + "/api/admin/event-sources/" + es.getId(), withAdminReq());
            }
        }
    }

}
