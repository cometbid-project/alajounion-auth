/**
 * 
 */
package com.alajounion.api.secure.rest.router;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alajounion.api.secure.rest.handler.AuthHandler;

/**
 * @author Gbenga
 *
 */
@Configuration
public class SecureAuthRouter {

	@Bean
	public RouterFunction<ServerResponse> route(AuthHandler authHandler) {
		return RouterFunctions.route(GET("/auth/v1/user").and(accept(APPLICATION_JSON)), authHandler::currentUser)
				.andRoute(GET("/auth/v1/systemUser").and(accept(APPLICATION_JSON)), authHandler::systemUser)
				.andRoute(
						PATCH("/auth/v1/renew/token").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)),
						authHandler::renewJwtToken)
				.andRoute(GET("/auth/v1/{id}/activate/token").and(accept(APPLICATION_JSON)),
						authHandler::getActivationToken)
				.andRoute(POST("/auth/v1/logout").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)),
						authHandler::logout)
				.andRoute(POST("/auth/v1/login").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)),
						authHandler::createAuthenticationToken);
	}
}
