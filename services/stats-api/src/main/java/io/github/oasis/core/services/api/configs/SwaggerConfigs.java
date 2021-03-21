/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.configs;

import io.github.oasis.core.services.ApiConstants;
import io.github.oasis.core.services.api.handlers.OasisErrorHandler;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class SwaggerConfigs {

    public static final String API_KEY_SCHEMA = "apiKeySchema";
    public static final String APP_KEY_SCHEMA = "appKeySchema";

    @Bean
    public OpenAPI oasisApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEMA, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(ApiConstants.APP_KEY_HEADER)
                        ).addSecuritySchemes(APP_KEY_SCHEMA, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(ApiConstants.APP_ID_HEADER)
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEMA).addList(APP_KEY_SCHEMA))
                .info(new Info().title("Oasis Admin/Stats API")
                        .description("Using this API, all admin operations and statistic queries can be done.")
                        .version("v1.0")
                        .license(new License().name("Apache 2.0"))
                );
    }

    @Bean
    public OpenApiCustomiser customizeOpenApi() {
        OasisErrorHandler.ErrorObject errorObject400 = new OasisErrorHandler.ErrorObject().toBuilder()
                .path("/context/path")
                .errorCode("E00001")
                .message("Error message shown here")
                .errorCodeDescription("Error code description goes here")
                .status(400)
                .build();
        OasisErrorHandler.ErrorObject errorObject401 = errorObject400.toBuilder()
                .status(401)
                .build();

        return openApi -> {
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();
                responses.addApiResponse("400", new ApiResponse().description("Oasis Bad Request Error")
                        .content(new Content()
                                .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                        new MediaType().example(errorObject400))));
                responses.addApiResponse("401", new ApiResponse().description("Oasis Auth Error")
                        .content(new Content()
                                .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                        new MediaType().example(errorObject401))));
            }));
        };
    }
}
