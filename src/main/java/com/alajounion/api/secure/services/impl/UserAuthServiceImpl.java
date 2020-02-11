/**
 * 
 */
package com.alajounion.api.secure.services.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.alajounion.api.secure.config.AuthProfile;
import com.alajounion.api.secure.domain.UserAuth;
import com.alajounion.api.secure.domain.mappers.UserMapper;
import com.alajounion.api.secure.domain.vo.UserVO;
import com.alajounion.api.secure.repository.UserRepository;
import com.alajounion.api.secure.services.UserAuthService;
import com.alajounion.api.secure.services.events.ProfileCreatedEvent;
import com.alajounion.api.server.models.ExpireStatusUpdateRequest;
import com.alajounion.api.server.models.LockStatusUpdateRequest;
import com.alajounion.api.server.models.PasswordUpdateRequest;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.exceptions.ApplicationDefinedRuntimeException;
import com.cometbid.project.common.exceptions.PasswordNotAcceptableException;
import com.cometbid.project.common.exceptions.UserAccountDisabledException;
import com.cometbid.project.common.exceptions.UserNotFoundException;
import com.cometbid.project.common.utils.PaginationHelper;
import com.cometbid.project.common.validators.qualifiers.PagingValidator;
import com.cometbid.project.security.CommonSecurity;
import com.cometbid.project.security.audit.Username;
import com.vladmihalcea.concurrent.Retry;
import com.alajounion.api.server.models.Count;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Slf4j
@Primary
@Service
@Validated
public class UserAuthServiceImpl implements UserAuthService {

	@Autowired
	ReactiveMongoTemplate template;

	@Autowired
	private AuthProfile dataStore;

	private final ApplicationEventPublisher publisher;
	private final CommonSecurity utils;
	private final UserRepository personRepository;

	public UserAuthServiceImpl(UserRepository personRepository, ApplicationEventPublisher publisher,
			CommonSecurity utils) {
		this.personRepository = personRepository;
		this.utils = utils;
		this.publisher = publisher;
	}

	/**
	 * Find all users paginated
	 * 
	 */
	@Override
	public Mono<Map<String, Object>> findAll(
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {
		try {
			final Map<String, Object> map = new HashMap<>();

			return personRepository.count().log().map(Count::new).flatMap(c -> {
				log.info("{} User auth record(s) found!", c.getCount());
				map.put("recordCount", c.getCount());

				int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

				Flux<UserVO> userVo = this.personRepository.findAll().skip(adjPageNo * pageSize).take(pageSize)
						.map(UserMapper::toViewObject).switchIfEmpty(Mono.empty());

				map.put("result", userVo);

				return Mono.just(map);
			});
			/*
			 * pageNo = PaginationHelper.adjustPageNo(pageNo);
			 * 
			 * Flux<UserVO> userVo = this.personRepository.findAll().skip(pageNo *
			 * pageSize).take(pageSize).map(UserMapper::toViewObject)
			 * .switchIfEmpty(Mono.empty());
			 * 
			 * map.put("result", userVo);
			 * 
			 * return Mono.just(map);
			 */
		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving all user Auth account", ex)
							.addContextValue("Page No", pageNo).addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Find User by id
	 * 
	 * @param id
	 * @return Mono<UserVO> User record found or empty if not found
	 */
	@Override
	public Mono<UserVO> findById(@NotBlank final String id) {
		try {
			return this.personRepository.findById(id).map(UserMapper::toViewObject).switchIfEmpty(Mono.empty());

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException("Unexpected error occured while retrieving user by id", ex)
					.addContextValue("User id ", id);
		}
	}

	/**
	 * Store new User
	 * 
	 * @param user
	 * @return Mono<UserVO> User record stored
	 */
	@Override
	@Transactional
	public Mono<UserVO> save(@NonNull @Valid final UserVO user) {

		try {
			String hash = utils.getEncoder().encode(user.getPassword());
			user.setPassword(hash);

			return personRepository.insert(UserMapper.create(user))
					.doOnSuccess(profile -> this.publisher.publishEvent(new ProfileCreatedEvent(profile)))
					.map(UserMapper::toViewObject);

		} catch (ConstraintViolationException ex) {
			throw ex;
		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException("Unexpected error occured while creating new User", exp);
			// .addContextValue("User: ", user);
		}
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Flux<UserVO> saveUsers(@Valid final Flux<@NotNull UserVO> itrUsers) {
		return personRepository.saveAll(itrUsers.map(user -> {
			String hash = utils.getEncoder().encode(user.getPassword());
			user.setPassword(hash);

			return UserMapper.create(user);
		})).map(UserMapper::toViewObject);

	}

	/**
	 * 
	 * @param id
	 * @param passwdUpd
	 * @return
	 */
	@Override
	@Transactional
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public Mono<UserVO> changePasswd(@NonNull @Valid final PasswordUpdateRequest passwdUpd) {

		try {

			return this.personRepository.findById(passwdUpd.getUserId())
					.switchIfEmpty(Mono.error(new UserNotFoundException(passwdUpd.getUserId()))).map(p -> {
						if (!isPasswordEqual(passwdUpd.getOldPassword(), p.getPassword())) {
							throw new UserNotFoundException("Username or Password does not match");
						}

						final int MAX_PASSWORD_HIS = (int) dataStore.getMaximumPasswordHistory();

						if (isPasswordUsedBefore(p, passwdUpd.getNewPassword())) {
							throw new PasswordNotAcceptableException(
									"Password cannot be the current or previous " + MAX_PASSWORD_HIS + " passwords");
						}

						p.saveOldPassword(MAX_PASSWORD_HIS);

						String hashedPassword = utils.getEncoder().encode(passwdUpd.getNewPassword());
						p.setPassword(hashedPassword);
						p.setStatus(StatusType.VALID.name());

						return p;
					}).switchIfEmpty(Mono.empty()).flatMap(personRepository::save).map(UserMapper::toViewObject);
		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException("Unexpected error occured while changing passwd", exp);
		}
	}

	/**
	 * 
	 * @param id
	 * @param username
	 * @param password
	 * @return Mono<UserVO>
	 */
	@Override
	public Mono<UserVO> authenticate(@NotBlank final String id, @NotBlank final String username,
			@NotBlank final String password) {

		// log on console
		log.info("Authentication service! ");

		try {

			// do put
			return this.personRepository.findByIdAndUsernameIgnoreCase(id, username)
					.switchIfEmpty(Mono.error(new BadCredentialsException("Incorrect username or password"))).map(p -> {
						String currentUser = p.getUsername();
						log.info("Current User...**************** {}", currentUser);

						if (username == null || !username.equalsIgnoreCase(currentUser)) {
							log.info("Username or Password does not match");
							throw new UserNotFoundException(
									"Username or Password does not match, pls try again with valid credentials");
						}

						if (!isPasswordEqual(password, p.getPassword())) {
							log.info("Username or Password does not match");
							throw new UserNotFoundException(
									"Username or Password does not match, pls try again with valid credentials");
						}
						if (!p.isAccountNonLocked()) {
							log.info("User account has been locked");
							throw new UserAccountDisabledException(
									"User account has been locked, pls contact your admin");
						}
						if (!p.isAccountNonExpired()) {
							log.info("User account has expired");
							throw new UserAccountDisabledException(
									"User account has expired, pls change your password to proceed.");
						}
						return p;
					}).map(UserMapper::toViewObject);

		} catch (RuntimeException exp) {
			exp.printStackTrace();

			throw new ApplicationDefinedRuntimeException("Unexpected error occured while authenticating user", exp);
		}
	}

	/**
	 * 
	 * @param id
	 * @param username
	 * @return
	 */
	@Override
	public Mono<UserVO> findByIdAndUsername(@NotBlank final String id, @NotBlank final String username) {
		try {
			return this.personRepository.findByIdAndUsernameIgnoreCase(id, username).switchIfEmpty(Mono.empty())
					.map(UserMapper::toViewObject);

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving user by id and username", ex)
							.addContextValue("User id ", id).addContextValue("username ", username);
		}
	}

	/**
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	@Override
	@Transactional
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public Mono<UserVO> lockUserStatus(@NotBlank final String id,
			@NonNull @Valid final LockStatusUpdateRequest status) {

		try {
			return this.personRepository.findById(id).switchIfEmpty(Mono.empty()).map(p -> {
				p.setStatus(status.isLock() ? StatusType.LOCKED.name() : StatusType.VALID.name());
				return p;
			}).switchIfEmpty(Mono.empty()).flatMap(personRepository::save).map(UserMapper::toViewObject);
		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while changing User record status(lock)", exp);
		}
	}

	/**
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	@Override
	@Transactional
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public Mono<UserVO> expireUserStatus(@NotBlank final String id,
			@NonNull @Valid final ExpireStatusUpdateRequest status) {

		log.info("Changing User Account Status....");
		try {
			return this.personRepository.findById(id).switchIfEmpty(Mono.empty()).map(p -> {
				p.setStatus(status.isExpire() ? StatusType.EXPIRED.name() : StatusType.VALID.name());
				return p;
			}).flatMap(personRepository::save).map(UserMapper::toViewObject);
		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while changing User record status(expire)", exp);
		}
	}

	private boolean isPasswordUsedBefore(UserAuth user, String newPassword) {

		String currentPasswd = user.getPassword();

		if (!isPasswordEqual(newPassword, currentPasswd)) {
			return user.getPasswrdHis().stream().anyMatch(p -> isPasswordEqual(newPassword, p.getHashedPassword()));
		}

		return true;
	}

	private boolean isPasswordEqual(@NotBlank String plainText, @NotBlank String hashedText) {
		log.info("Password check done....****************");
		log.info("Plain text Password**************** {}", plainText);
		log.info("Encoded Password**************** {}", hashedText);
		log.info("Encoder used {}", utils.getEncoder().getClass().getName());

		boolean result = utils.getEncoder().matches(plainText, hashedText);

		log.info("Password match**************** {}", result);
		return result;
	}

	/**
	 * Invoked by the Scheduler to expire Batch of Users due for Password
	 * expiration. Expired Users will be required to login
	 */
	@Transactional
	@Async("threadPoolTaskExecutor")
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public void expirePasswordRecords() {
		try {
			// To do:
			long EXPIRATION_PERIOD = dataStore.getPasswordExpirationPeriod();

			log.info("EXPIRATION_PERIOD Value: {}", EXPIRATION_PERIOD);

			LocalDateTime lastExpiryDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(EXPIRATION_PERIOD)
					.truncatedTo(ChronoUnit.DAYS);

			Query query = new Query();
			query.addCriteria(
					Criteria.where("EXPIRED_TIME").lte(lastExpiryDate).and("STATUS").is(StatusType.VALID.name()));

			Update update = new Update();

			// update age to 11
			update.set("STATUS", StatusType.EXPIRED.name());
			update.set("EXPIRED_TIME", LocalDateTime.now(ZoneOffset.UTC));

			// update all matched, both 1004 and 1005
			this.template.updateMulti(query, update, UserAuth.class).log("Scheduled User Account Expiration")
					.map(p -> p.getModifiedCount()).subscribe(c -> log.info("{} User record(s) expired!", c));

		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException("An unexpected error occured while expiring User Credentials",
					exp);
		}
	}

	/**
	 * MVC Pattern/Approach
	 * 
	 * @return
	 */
	public Mono<Username> getCurrentUser(String mvc) {
		Optional<User> user = Optional.ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication).filter(Authentication::isAuthenticated)
				.map(Authentication::getPrincipal).map(User.class::cast);

		String roles = null;
		String username = "Guest, you are not logged in";

		Username authUser = null;
		if (user.isPresent()) {
			username = user.get().getUsername();
			roles = user.get().getAuthorities().parallelStream().map(auth -> (GrantedAuthority) auth)
					.map(a -> a.getAuthority()).collect(Collectors.joining(","));
			authUser = new Username(username, roles);
		}
		return Mono.justOrEmpty(authUser);
	}

	/**
	 * Reactive Pattern/Approach
	 * 
	 * @return
	 */
	public Mono<Username> getCurrentUser() {
		return ReactiveSecurityContextHolder.getContext().map(p -> p.getAuthentication()).map(auth -> {
			Principal user = (Principal) auth.getPrincipal();
			String username = user.getName();
			String roles = auth.getAuthorities().parallelStream().map(role -> (GrantedAuthority) role)
					.map(a -> a.getAuthority()).collect(Collectors.joining(","));
			return new Username(username, roles);
		});
	}

	/**
	 * 
	 * @return
	 */
	public Mono<Object> clearAllRecords() {
		return this.personRepository.count().flatMap(c -> {
			if (c != 0L) {
				return personRepository.deleteAll().map(p -> {
					return Mono.just("Successful");
				});
			}
			return Mono.just("No record found to delete");
		});
	}

}
