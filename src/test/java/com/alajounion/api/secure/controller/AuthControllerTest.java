/**
 * 
 */
package com.alajounion.api.secure.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.alajounion.api.secure.config.MongoConfig;
import com.alajounion.api.secure.config.SpringSecurityWebFluxConfig;
//import com.alajounion.api.secure.config.MongoConfig;
//import com.alajounion.api.secure.config.SpringSecurityWebFluxConfig;
import com.alajounion.api.secure.domain.AppPermission;
import com.alajounion.api.secure.domain.AppRole;
import com.alajounion.api.secure.domain.vo.SuccessLoginVO;
import com.alajounion.api.secure.rest.handler.AuthHandler;
import com.alajounion.api.secure.services.LoginService;
import com.alajounion.api.secure.services.UserAuthService;
import com.alajounion.api.secure.services.impl.LoginServiceImpl;
import com.alajounion.api.secure.services.impl.UserAuthServiceImpl;
import com.alajounion.api.server.models.AuthenticationRequest;
import com.alajounion.api.server.models.ExpireStatusUpdateRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AuthHandler.class)
@ContextConfiguration(classes = { SpringSecurityWebFluxConfig.class, MongoConfig.class, })
@Import(LoginService.class)
class AuthControllerTest {

	@Configuration
	static class Config {

		
	}

	@MockBean
	LoginService lgService;

	@MockBean
	private ReactiveUserDetailsService userDetailsService;

	@MockBean
	private UserAuthService userService;

	@Autowired
	private WebTestClient webClient;

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

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.alajounion.api.secure.rest.handler.AuthHandler#createAuthenticationToken(com.alajounion.api.server.models.AuthenticationRequest, java.lang.String)}.
	 */
	// @Test
	void testCreateAuthenticationToken() {
		AuthenticationRequest authReq = new AuthenticationRequest();
		authReq.setId(UUID.randomUUID().toString());
		authReq.setPassword("password");
		authReq.setUsername("aUser");
		authReq.setIpAddress("186.109.218.249");

		// Step 1 - create the mock object
		//HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		// Step 2 - record the expected behavior		
		// Mockito.when(request.getHeader("user-agent")).thenReturn("Just a Test");

		// program the mock to return false for param2
		// Mockito.when(request.getParameter("param2")).thenReturn("false");

		Mockito.when(lgService.recordSuccessLogin(new SuccessLoginVO())).thenReturn(Mono.empty());

		Mockito.when(userService.expireUserStatus(authReq.getId(), new ExpireStatusUpdateRequest(false)))
				.thenReturn(Mono.empty());

		AppRole[] array = { new AppRole("ADMIN") };
		final List<AppRole> collRole = Stream.of(array).collect(Collectors.toList());

		// Collection<AppRole> collRole = new
		// ArrayList<AppRole>().addAll(Arrays.asList(array)));

		UserDetails user = User.withUsername(authReq.getUsername()).accountLocked(false).disabled(false)
				.password(authReq.getPassword()).authorities(getAuthorities(collRole)).build();

		Mockito.when(userDetailsService.findByUsername(authReq.getId())).thenReturn(Mono.just(user));

		// Mockito.when(userService.authenticate(authReq.getId(), authReq.getUsername(), authReq.getPassword()))
			//	.thenReturn(Mono.empty());

		Mockito.when(lgService.incrementFailedLogins(authReq.getIpAddress())).thenReturn(Flux.empty());

		webClient.post().uri("/auth/v1/login").contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(authReq)).exchange().expectStatus().isOk().expectBody().jsonPath("$.jwt")
				.isNotEmpty().jsonPath("$.username").isEqualTo("aUser");

	}

	/**
	 * Test method for
	 * {@link com.alajounion.api.secure.rest.handler.AuthHandler#currentUser(reactor.core.publisher.Mono)}.
	 */
	// @Test
	void testCurrentUser() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.alajounion.api.secure.rest.handler.AuthHandler#systemUser()}.
	 */
	// @Test
	void testSystemUser() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.alajounion.api.secure.rest.handler.AuthHandler#logout( org.springframework.web.server.WebSession, com.alajounion.api.server.models.AuthenticationRequest)}.
	 */
	// @Test
	void testLogout() {
		fail("Not yet implemented");
	}

	private Collection<? extends GrantedAuthority> getAuthorities(Collection<AppRole> roles) {
		List<GrantedAuthority> authorities = new ArrayList<>();

		for (AppRole role : roles) {
			authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

			/*
			Optional<Set<AppPermission>> userPermissions = Optional.ofNullable(role.getGroupAuthorities());
			if (userPermissions.isPresent()) {
				userPermissions.get().stream().map(p -> new SimpleGrantedAuthority(p.getPermission()))
						.forEach(authorities::add);
			}
			*/
		}

		return authorities;
	}

}
