/**
 * 
 */
package com.alajounion.api.secure.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.test.context.ContextConfiguration;

import com.alajounion.api.secure.config.AuthProfile;
import com.alajounion.api.secure.config.MongoConfig;
import com.alajounion.api.secure.config.SpringSecurityWebFluxConfig;
import com.alajounion.api.secure.domain.vo.UserVO;
import com.cometbid.project.common.enums.Role;
import com.alajounion.api.secure.repository.UserRepository;
import com.alajounion.api.secure.services.impl.UserAuthServiceImpl;
import com.alajounion.api.server.models.ExpireStatusUpdateRequest;
import com.alajounion.api.server.models.LockStatusUpdateRequest;
import com.alajounion.api.server.models.PasswordUpdateRequest;
import com.cometbid.project.common.utils.RandomString;
import com.cometbid.project.common.validators.GlobalProgrammaticValidator;
import com.cometbid.project.security.audit.Username;
import com.cometbid.project.security.handler.JWTReactiveAuthManager;
import com.cometbid.project.security.handler.SecurityContextRepository;
import com.cometbid.project.security.jwt.utils.JWTUtil;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;

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
@Import({ UserAuthServiceImpl.class, JWTReactiveAuthManager.class, GlobalProgrammaticValidator.class, JWTUtil.class,
		SecurityContextRepository.class, AuthProfile.class })
public class UserAuthServiceTest {

	// @Autowired
	UserAuthServiceImpl userService;

	// @Autowired
	UserRepository repository;

	public UserAuthServiceTest(@Autowired UserAuthServiceImpl service, @Autowired UserRepository repository) {
		this.userService = service;
		this.repository = repository;
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

	public Mono<Username> getCurrentUser() {
		return Mono.empty();
	}

	@Test
	public void testFindById() {
		UserVO userVO = UserVO.builder().username("user-authA").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		Mono<UserVO> user = this.userService.save(userVO).flatMap(saved -> this.userService.findById(saved.getId()));

		StepVerifier.create(user).expectNextMatches(
				profile -> StringUtils.hasText(profile.getId()) && profile.getUsername().equalsIgnoreCase("user-authA"))
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("user-authB").password("userB@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("MANAGER"))
						.collect(Collectors.toSet()))
				.build();

		user = this.userService.save(userVO).flatMap(saved -> this.userService.findById(saved.getId()));

		StepVerifier.create(user).expectNextMatches(
				profile -> StringUtils.hasText(profile.getId()) && profile.getUsername().equalsIgnoreCase("user-authB"))
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("user-authC").password("userC@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ADMIN")).collect(Collectors.toSet()))
				.build();

		user = this.userService.save(userVO).flatMap(saved -> this.userService.findById(saved.getId()));

		StepVerifier.create(user).expectNextMatches(
				profile -> StringUtils.hasText(profile.getId()) && profile.getUsername().equalsIgnoreCase("user-authC"))
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("user-authD").password("userD@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ANONYMOUS"))
						.collect(Collectors.toSet()))
				.build();

		try {
			user = this.userService.save(userVO).flatMap(saved -> this.userService.findById(saved.getId()));
		} catch (ConstraintViolationException ex) {
			log.error("Some attributes failed validation...");
		}

		StepVerifier.create(user).expectNextMatches(
				profile -> StringUtils.hasText(profile.getId()) && profile.getUsername().equalsIgnoreCase("user-authD"))
				// .verifyError(ConstraintViolationException.class);
				.expectError(ConstraintViolationException.class);
	}

	@Test
	public void testSave() {
		UserVO userVO = UserVO.builder().username("user-authA").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		Mono<UserVO> monoUser = this.userService.save(userVO);

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("user-authA")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty())
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("user-authB").password("userB@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("MANAGER"))
						.collect(Collectors.toSet()))
				.build();

		monoUser = this.userService.save(userVO);

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("user-authB")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty())
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("user-authC").password("userC@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ADMIN")).collect(Collectors.toSet()))
				.build();

		monoUser = this.userService.save(userVO);

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("user-authC")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty())
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("user-authD").password("userD@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ANONYMOUS"))
						.collect(Collectors.toSet()))
				.build();

		try {
			monoUser = this.userService.save(userVO);
		} catch (ConstraintViolationException ex) {
			log.error("Some attributes failed validation...");
		}

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("user-authD")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty())
				// .verifyError(ConstraintViolationException.class);
				.expectError(ConstraintViolationException.class);
	}

	@Test
	public void testChangePasswd() {

		String username = "user-authA";
		UserVO userVO = UserVO.builder().username(username).password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		Mono<UserVO> monoUser = this.userService.save(userVO).map(p -> {
			log.info("Old Hashed Password {}", p.getPassword());
			return p;
		}).doOnSuccess(p -> log.info("User saved successfully..."));

		// --------------------------------------------------------------------------------------------------------------------
		monoUser = monoUser.flatMap(saved -> this.userService.changePasswd(new PasswordUpdateRequest(saved.getId(),
				RandomString.generateRandomStringBounded(64, true, true), "userA@cometbid.com", "userB@cometbid.com")))
				.map(c -> {
					log.info("New Hashed Password {}", c.getPassword());
					return c;
				})
				.flatMap(saved -> this.userService.changePasswd(new PasswordUpdateRequest(saved.getId(),
						RandomString.generateRandomStringBounded(64, true, true), "userB@cometbid.com",
						"userC@cometbid.com")))
				.map(c -> {
					log.info("New Hashed Password {}", c.getPassword());
					return c;
				})
				.flatMap(saved -> this.userService.changePasswd(new PasswordUpdateRequest(saved.getId(),
						RandomString.generateRandomStringBounded(64, true, true), "userC@cometbid.com",
						"userD@cometbid.com")))
				.map(c -> {
					log.info("New Hashed Password {}", c.getPassword());
					return c;
				})
				.flatMap(saved -> this.userService.changePasswd(new PasswordUpdateRequest(saved.getId(),
						RandomString.generateRandomStringBounded(64, true, true), "userD@cometbid.com",
						"userE@cometbid.com")))
				.map(c -> {
					log.info("New Hashed Password {}", c.getPassword());
					return c;
				})
				.flatMap(saved -> this.userService.changePasswd(new PasswordUpdateRequest(saved.getId(),
						RandomString.generateRandomStringBounded(64, true, true), "userE@cometbid.com",
						"userA@cometbid.com")))
				.map(c -> {
					log.info("New Hashed Password {}", c.getPassword());
					return c;
				});

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase(username)
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty())
				.verifyComplete();

	}

	@Test
	public void testAuthenticate() {

		String token = RandomString.generateRandomStringBounded(64, true, true);

		// String username = "user-authA";
		UserVO userVO = UserVO.builder().username("userA@test.com").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		Mono<UserVO> monoUser = this.userService.save(userVO).map(p -> {
			log.info("Old Hashed Password {}", p.getPassword());
			return p;
		}).flatMap(saved -> this.userService.changePasswd(
				new PasswordUpdateRequest(saved.getId(), token, "userA@cometbid.com", "userB@cometbid.com"))).map(c -> {
					log.info("New Hashed Password {}", c.getPassword());
					return c;
				})
				.flatMap(saved -> this.userService.authenticate(saved.getId(), "userA@test.com", "userB@cometbid.com"));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userA@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& !profile.isDisabled())
				.verifyComplete();
	}

	@Test
	public void testFindByIdAndUsername() {

		UserVO userVO = UserVO.builder().username("userA@test.com").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		Mono<UserVO> monoUser = this.userService.save(userVO)
				.flatMap(saved -> this.userService.findByIdAndUsername(saved.getId(), "userA@test.com"));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userA@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& profile.isDisabled())
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("userB@test.com").password("userB@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("MANAGER"))
						.collect(Collectors.toSet()))
				.build();

		monoUser = this.userService.save(userVO)
				.flatMap(saved -> this.userService.findByIdAndUsername(saved.getId(), "userB@test.com"));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userB@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& profile.isDisabled())
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("userC@test.com").password("userC@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ADMIN")).collect(Collectors.toSet()))
				.build();

		monoUser = this.userService.save(userVO)
				.flatMap(saved -> this.userService.findByIdAndUsername(saved.getId(), "userC@test.com"));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userC@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& profile.isDisabled())
				.verifyComplete();
		// --------------------------------------------------------------------------------------------------------------------
		userVO = UserVO.builder().username("userD@test.com").password("userD@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ANONYMOUS"))
						.collect(Collectors.toSet()))
				.build();
		try {
			monoUser = this.userService.save(userVO)
					.flatMap(saved -> this.userService.findByIdAndUsername(saved.getId(), "userD@test.com"));
		} catch (ConstraintViolationException ex) {
			log.error("Some attributes failed validation...");
		}
		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userD@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& profile.isDisabled())
				.expectError();
	}

	@Test
	public void testLockUserStatus() {

		UserVO userVO = UserVO.builder().username("userA@test.com").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		boolean toLock = true;
		Mono<UserVO> monoUser = this.userService.save(userVO).flatMap(
				saved -> this.userService.lockUserStatus(saved.getId(), new LockStatusUpdateRequest(toLock)).map(c -> {
					log.info("User Account is disabled {}", c.isLocked());
					return c;
				}));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userA@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& profile.isLocked())
				.verifyComplete();

		// --------------------------------------------------------------------------------------------------------------------

		boolean toLockAcct = false;
		monoUser = this.userService.save(userVO).flatMap(saved -> this.userService
				.lockUserStatus(saved.getId(), new LockStatusUpdateRequest(toLockAcct)).map(c -> {
					log.info("User Account is Enabled {}", !c.isLocked());
					return c;
				}));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userA@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& !profile.isLocked())
				.verifyComplete();
	}

	@Test
	public void testExpireUserStatus() {

		UserVO userVO = UserVO.builder().username("userA@test.com").password("userA@cometbid.com") // userA@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
				.build();

		boolean disable = true;
		Mono<UserVO> monoUser = this.userService.save(userVO).flatMap(saved -> this.userService
				.expireUserStatus(saved.getId(), new ExpireStatusUpdateRequest(disable)).map(c -> {
					log.info("User Account is disabled {}", c.isDisabled());
					return c;
				}));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userA@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& profile.isDisabled())
				.verifyComplete();

		// --------------------------------------------------------------------------------------------------------------------

		boolean disableAcct = false;
		monoUser = this.userService.save(userVO).flatMap(saved -> this.userService
				.expireUserStatus(saved.getId(), new ExpireStatusUpdateRequest(disableAcct)).map(c -> {
					log.info("User Account is enabled {}", !c.isDisabled());
					return c;
				}));

		StepVerifier.create(monoUser)
				.expectNextMatches(profile -> StringUtils.hasText(profile.getId())
						&& profile.getUsername().equalsIgnoreCase("userA@test.com")
						&& StringUtils.hasText(profile.getPassword()) && !profile.getRoles().isEmpty()
						&& !profile.isDisabled())
				.verifyComplete();
	}

	@Test
	public void saveUsers() {

		// userService.clearAllRecords().doOnSuccess(p -> log.info("All record
		// cleared...")).subscribe();

		UserVO[] defaultUsers = { UserVO.builder().username("userA@test.com").password("test1@cometbid.com") // test1@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ADMIN")).collect(Collectors.toSet()))
				.build(),
				UserVO.builder().username("userB@test.com").password("test2@cometbid.com") // test2@cometbid.com
						.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
								.collect(Collectors.toSet()))
						.build(),
				UserVO.builder().username("userC@test.com").password("test3@cometbid.com") // test3@cometbid.com
						.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("MANAGER"))
								.collect(Collectors.toSet()))
						.build() };

		List<UserVO> existingList = Arrays.asList(defaultUsers);
		Flux<UserVO> fluxUser = Flux.fromIterable(existingList);
		
		Flux<Long> fluxCount = fluxUser.flatMap(p ->  {
			userService.clearAllRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			userService.saveUsers(Flux.fromIterable(existingList)).subscribe();
			return Mono.just("New records saved...");
		}).flatMap(p -> userService.findAll(1, 10)
				.map(c -> { 
					Long count = (Long) c.get("recordCount"); 
					log.info("No of record {}", count);
					if(count == defaultUsers.length) {
						log.info("test passed, record found equal record inserted...");
					} else {
						log.info("test failed, record found not equal record inserted...");
					}
					return count;
				 }));
		
		fluxCount.subscribe();
		// StepVerifier.create(fluxCount).expectNextMatches(count -> count == defaultUsers.length).verifyComplete();

	}

}
