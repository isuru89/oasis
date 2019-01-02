package io.github.isuru.oasis.services.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class TokenSecurityFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(TokenSecurityFilter.class);

    private static final String BEARER = "Bearer";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BASIC = "Basic";

    private static final String OASIS_ISSUER = "oasis";

    private JWTVerifier verifier;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        OasisConfigurations configurations = null;
        if (webApplicationContext != null) {
            configurations = webApplicationContext.getBean(OasisConfigurations.class);
        }

        if (configurations == null) {
            throw new ServletException("Cannot find Oasis configurations for the filter!");
        }

        String publicKeyPath = configurations.getPublicKeyPath();
        String privateKeyPath = configurations.getPrivateKeyPath();
        if (publicKeyPath == null || privateKeyPath == null) {
            throw new ServletException("Required authentication keys are not specified in configurations!");
        }

        if (!new File(privateKeyPath).exists() || !new File(publicKeyPath).exists()) {
            throw new ServletException("Required authenticate keys not found in the deployment!");
        }

        try {
            byte[] bytesPrivate = Files.readAllBytes(Paths.get(privateKeyPath));
            byte[] bytesPublic = Files.readAllBytes(Paths.get(publicKeyPath));
            PKCS8EncodedKeySpec specPrivate = new PKCS8EncodedKeySpec(bytesPrivate);
            X509EncodedKeySpec specPublic = new X509EncodedKeySpec(bytesPublic);
            RSAPrivateKey rsaPrivate = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(specPrivate);
            RSAPublicKey rsaPublic = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(specPublic);

            Algorithm algorithm = Algorithm.RSA256(rsaPublic, rsaPrivate);
            verifier = JWT.require(algorithm)
                    .withIssuer(OASIS_ISSUER)
                    .build();

            LOG.info("JWT authenticator initialized.");

        } catch (IOException e) {
            throw new ServletException("Failed to read necessary key filed required for JWT authentication!");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ServletException("Unable to initialize JWT algorithms!");
        }
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        String authHeader = req.getHeader(AUTHORIZATION);
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            if (authHeader.startsWith(BEARER)) {
                String token = authHeader.substring(BEARER.length());

                try {
                    DecodedJWT jwt = verifier.verify(token);
                    AuthUtils.TokenInfo tokenInfo = new AuthUtils.TokenInfo();
                    tokenInfo.setUser(jwt.getClaim("user").asLong());
                    tokenInfo.setRole(jwt.getClaim("role").asInt());
                    tokenInfo.setIssuedAt(jwt.getIssuedAt().getTime());
                    tokenInfo.setExp(jwt.getExpiresAt().getTime());

                    req.setAttribute("token", tokenInfo);
                    req.setAttribute("userId", tokenInfo.getUser()); // set user
                    filterChain.doFilter(request, response);

                } catch (JWTVerificationException e) {
                    HttpServletResponse res = (HttpServletResponse) response;
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                            "You are not allowed to access end point! Provided token is invalid!");
                }
            }

        } else {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "You are not allowed to access end point!");
        }
    }
}
