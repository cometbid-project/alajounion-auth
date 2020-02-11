/**
 * 
 */
package com.alajounion.api.secure.rest.router;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alajounion.api.secure.rest.handler.LoginHandler;

/**
 * @author Gbenga
 *
 */
@Configuration
public class LoginRouter {

	@Bean
	public RouterFunction<ServerResponse> route(LoginHandler loginHandler) {
		return RouterFunctions.route(GET("/login/v1/success").and(accept(APPLICATION_JSON)), loginHandler::allSuccessfulLogins)
				.andRoute(GET("/login/v1/failed").and(accept(APPLICATION_JSON)), loginHandler::allFailedLogins)
				.andRoute(GET("/login/v1/success/ip/{ipAddr}").and(accept(APPLICATION_JSON)),
						loginHandler::allSuccessfulLoginsByIPAddr)
				.andRoute(GET("/login/v1/success/ip/{ip}/op/{op}/username/{username}").and(accept(APPLICATION_JSON)),
						loginHandler::allFailedLoginsByIpAddr)
				.andRoute(GET("/login/v1/{id}/{username}").and(accept(APPLICATION_JSON)),
						loginHandler::successfulLoginByIPUsername)
				.andRoute(GET("/login/v1/{id}/{username}").and(accept(APPLICATION_JSON)),
						loginHandler::successfulLoginByIdUsername)
				.andRoute(GET("/login/v1/search").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)),
						loginHandler::searchLoginByDate);
	}
}
