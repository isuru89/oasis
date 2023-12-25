package io.github.oasis.core.services.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.services.api.configs.ErrorMessages;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.handlers.OasisErrorHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.time.Instant;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class OasisErrorHandlingConfigs {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ErrorMessages errorMessages;

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (httpServletRequest, httpServletResponse, e) -> {
            OasisErrorHandler.ErrorObject errorObject = new OasisErrorHandler.ErrorObject();
            errorObject.setTimestamp(Instant.now().toString());
            errorObject.setErrorCode(ErrorCodes.AUTH_BAD_CREDENTIALS);
            errorObject.setStatus(HttpStatus.FORBIDDEN.value());
            errorObject.setErrorCodeDescription(errorMessages.getErrorMessage(errorObject.getErrorCode()));
            errorObject.setMessage(e.getMessage());
            errorObject.setPath(httpServletRequest.getRequestURI());

            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            MAPPER.writeValue(httpServletResponse.getOutputStream(), errorObject);
            httpServletResponse.getOutputStream().flush();
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (httpServletRequest, httpServletResponse, e) -> {
            OasisErrorHandler.ErrorObject errorObject = new OasisErrorHandler.ErrorObject();
            errorObject.setTimestamp(Instant.now().toString());
            errorObject.setErrorCode(ErrorCodes.AUTH_NO_SUCH_CREDENTIALS);
            errorObject.setStatus(HttpStatus.UNAUTHORIZED.value());
            errorObject.setErrorCodeDescription(errorMessages.getErrorMessage(errorObject.getErrorCode()));
            errorObject.setMessage(e.getMessage());
            errorObject.setPath(httpServletRequest.getRequestURI());

            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            MAPPER.writeValue(httpServletResponse.getOutputStream(), errorObject);
            httpServletResponse.getOutputStream().flush();
        };
    }

}
