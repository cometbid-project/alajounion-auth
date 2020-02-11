/**
 * 
 */
package com.alajounion.api.secure.rest.router;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alajounion.api.secure.rest.handler.UserAuthHandler;

/**
 * @author Gbenga
 *
 */
@Configuration
public class AuthRouter {

	@Bean
	public RouterFunction<ServerResponse> route(UserAuthHandler authHandler) {
		return RouterFunctions.route(GET("/passwd/v1/{id}").and(accept(APPLICATION_JSON)), authHandler::getById)
				.andRoute(GET("/passwd/v1/{id}/username/{username}").and(accept(APPLICATION_JSON)),
						authHandler::getByIdUsername)
				.andRoute(POST("/passwd/v1").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)),
						authHandler::createUser)
				.andRoute(PUT("/passwd/v1/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)),
						authHandler::changePasswd)
				.andRoute(PUT("/passwd/v1/{id}/lock").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)),
						authHandler::lockUserPasswordAcct);
	}

}
