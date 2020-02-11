package com.alajounion.api.secure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.cometbid.project.security.annotation.Algorithm;
import com.cometbid.project.security.annotation.EnableCommonSecurity;
import com.cometbid.project.security.handler.JWTReactiveAuthManager;
import com.cometbid.project.security.handler.SecurityContextRepository;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableCommonSecurity(algorithm = Algorithm.BCRYPT)
@EnableReactiveMethodSecurity
public class SpringSecurityWebFluxConfig {

	@Autowired
	private ReactiveAuthenticationManager authManager;

	@Autowired
	private SecurityContextRepository securityContextRepository;

	// private static final String[] WHITELISTED_AUTH_URLS = { "/login", "/"};

	/**
	 * The test defined in SampleApplicationTests class will only get executed if
	 * you change the authentication mechanism to basic (from form mechanism) in
	 * SpringSecurityWebFluxConfig file
	 * 
	 * @param http
	 * @return
	 * @throws Exception
	 */
	@Bean
	public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity http) {
		return http.exceptionHandling().authenticationEntryPoint((swe, e) -> {
			return Mono.fromRunnable(() -> {
				swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			});
		}).accessDeniedHandler((swe, e) -> {
			return Mono.fromRunnable(() -> {
				swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
			});
		}).and().csrf().disable().formLogin().disable().httpBasic().disable()
				.authenticationManager(authManager).securityContextRepository(securityContextRepository)
				.authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll()
				.pathMatchers(HttpMethod.POST, "/auth/v1/login").permitAll()
				// .pathMatchers(WHITELISTED_AUTH_URLS).permitAll()
				.anyExchange().authenticated().and().build();
	}

}
