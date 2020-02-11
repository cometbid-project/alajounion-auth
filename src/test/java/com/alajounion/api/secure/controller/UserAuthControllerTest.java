/**
 * 
 */
package com.alajounion.api.secure.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.alajounion.api.secure.config.MongoConfig;
import com.alajounion.api.secure.config.SpringSecurityWebFluxConfig;
import com.alajounion.api.secure.domain.AppRole;
import com.alajounion.api.secure.domain.UserAuth;
import com.alajounion.api.secure.domain.vo.SuccessLoginVO;
import com.alajounion.api.secure.domain.vo.UserVO;
import com.alajounion.api.secure.rest.handler.UserAuthHandler;
import com.alajounion.api.secure.services.LoginService;
import com.alajounion.api.secure.services.RoleService;
import com.alajounion.api.secure.services.UserAuthService;
import com.alajounion.api.secure.services.impl.LoginServiceImpl;
import com.alajounion.api.secure.services.impl.RoleServiceImpl;
import com.alajounion.api.secure.services.impl.UserAuthServiceImpl;
import com.alajounion.api.server.models.ExpireStatusUpdateRequest;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserAuthHandler.class)
@ContextConfiguration(classes = { SpringSecurityWebFluxConfig.class, MongoConfig.class, })
@Import(UserAuthService.class)
class UserAuthControllerTest {

	@MockBean
	private UserAuthService userService;

	private String[] defaultRoles = { "SECRETARY", "MANAGER", "ADMIN" };

	private static final String TOKEN = "Bearer ";

	@Autowired
	WebTestClient webTestClient;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	private UserVO getDefaultAdminUser() {
		HashSet<String> arraySet = new HashSet<>();
		Collections.addAll(arraySet, defaultRoles[2]);

		return UserVO.builder().username("test4").password("test4@cometbid.com").roles(arraySet).build();
	}

	private void loginDefaultUser() {
		
		webTestClient.post()
				.uri("http://localhost:8082/auth/v1/login")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", TOKEN)
				.body(Mono.just(getDefaultAdminUser()), UserVO.class)
		.exchange().expectHeader().contentType(MediaType.APPLICATION_JSON).expectStatus().isCreated();

	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		// Step 1 - create the mock object

		// Step 2 - record the expected behavior

		// program the mock to return false for param2
		// Mockito.when(request.getParameter("param2")).thenReturn("false");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	// Create about Ten dummy Users
	@Test
	void testCreateUser() {

		loginDefaultUser();

		HashSet<String> arraySet = new HashSet<>();
		Collections.addAll(arraySet, defaultRoles[0]);

		UserVO user = UserVO.builder().username("userA").password("userA@cometbid.com").roles(arraySet).build();

		Mono<UserVO> userMono = Mono.just(user);
		when(userService.save(user)).thenReturn(userMono);

		webTestClient.post().uri("/passwd/v1").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).header("Authorization", TOKEN).body(Mono.just(user), UserVO.class)
				.exchange().expectHeader().contentType(MediaType.APPLICATION_JSON).expectStatus().isCreated()
				.expectBody().jsonPath("$.id").isNotEmpty().jsonPath("$.username").isEqualTo("test1");
		// .value(user1 -> user.getUsername(), equalTo("test1"));

	}

}
