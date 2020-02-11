/**
 * 
 */
package com.alajounion.api.secure.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;
import com.cometbid.project.common.utils.ActivationToken;
import com.alajounion.api.secure.config.AuthProfile;
import com.alajounion.api.secure.config.MongoConfig;
import com.alajounion.api.secure.config.SpringSecurityWebFluxConfig;
import com.alajounion.api.secure.domain.vo.UserVO;
import com.cometbid.project.common.enums.Role;
import com.alajounion.api.secure.repository.UserRepository;
import com.alajounion.api.secure.services.impl.ActivationTokenServiceImpl;
import com.alajounion.api.secure.services.impl.UserAuthServiceImpl;
import com.cometbid.project.common.validators.GlobalProgrammaticValidator;
import com.cometbid.project.security.handler.JWTReactiveAuthManager;
import com.cometbid.project.security.handler.SecurityContextRepository;
import com.cometbid.project.security.jwt.utils.JWTUtil;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Slf4j
@DataMongoTest
@ContextConfiguration(classes = { SpringSecurityWebFluxConfig.class, MongoConfig.class })
@Import({ ActivationTokenServiceImpl.class, UserAuthServiceImpl.class, JWTReactiveAuthManager.class,
		GlobalProgrammaticValidator.class, JWTUtil.class, SecurityContextRepository.class, AuthProfile.class })
public class ActivationServiceTest {

	UserAuthServiceImpl userService;
	ActivationTokenServiceImpl activationService;

	public ActivationServiceTest(@Autowired ActivationTokenServiceImpl service,
			@Autowired UserAuthServiceImpl userService) {
		this.activationService = service;
		this.userService = userService;
	}

	@Configuration
	static class Config {

		@Bean
		public ErrorResponseComposer errorResponseComposer() {
			List<AbstractExceptionHandler> handlers = new ArrayList<>();

			return new ErrorResponseComposer(handlers);
		}

		@Bean
		public ReactiveAuthenticationManager authManager() {

			return new JWTReactiveAuthManager();
		}

		@Bean
		public SecurityContextRepository securityContextRepository() {

			return new SecurityContextRepository();
		}

	}

	@Test
	public void testGenerateActivationToken() {
		UserVO userVO = UserVO.builder().username("user-authA").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		Mono<ActivationToken> monoToken = this.userService.save(userVO)
				.flatMap(p -> activationService.generateActivationToken(p.getId()));

		StepVerifier.create(monoToken).expectNextMatches(profile -> StringUtils.hasText(profile.getToken()))
				.verifyComplete();
	}

	@Test
	public void testValidateActivationToken() {
		UserVO userVO = UserVO.builder().username("user-authA").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		Mono<Boolean> monoToken = this.userService.save(userVO)
				.flatMap(p -> activationService.generateActivationToken(p.getId())
						.flatMap(m -> activationService.validateActivationToken(m.getUserId(), m.getToken())));

		StepVerifier.create(monoToken).expectNextMatches(profile -> profile == Boolean.TRUE).verifyComplete();

	}

}
