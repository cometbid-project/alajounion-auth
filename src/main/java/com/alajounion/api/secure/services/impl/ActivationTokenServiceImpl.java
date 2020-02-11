/**
 * 
 */
package com.alajounion.api.secure.services.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.alajounion.api.secure.config.AuthProfile;
import com.alajounion.api.secure.domain.UserActivation;
import com.alajounion.api.secure.domain.mappers.UserActivationMapper;
import com.alajounion.api.secure.repository.UserActivationTokenRepository;
import com.alajounion.api.secure.repository.UserRepository;
import com.alajounion.api.secure.services.ActivationTokenService;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.exceptions.ApplicationDefinedRuntimeException;
import com.cometbid.project.common.exceptions.UserNotFoundException;
import com.cometbid.project.common.utils.ActivationToken;
import com.cometbid.project.common.utils.RandomString;
import com.vladmihalcea.concurrent.Retry;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Slf4j
@Primary
@Service
@Validated
public class ActivationTokenServiceImpl implements ActivationTokenService {

	@Autowired
	ReactiveMongoTemplate template;

	@Autowired
	private AuthProfile dataStore;

	private final UserRepository personRepository;
	private final UserActivationTokenRepository tokenRepository;

	private final static int[] lengthArray = { 66, 72, 67, 77, 55 };
	private static Random random = new Random();

	public ActivationTokenServiceImpl(UserActivationTokenRepository tokenRepository, UserRepository personRepository) {
		this.tokenRepository = tokenRepository;
		this.personRepository = personRepository;
	}

	@Override
	@Transactional
	public Mono<ActivationToken> generateActivationToken(@NotBlank(message = "{userId.notBlank}") final String userId) {
		// TODO Auto-generated method stub

		Mono<UserActivation> alternativeMono = Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
			int length = lengthArray[random.nextInt(5)];
			boolean useLetters = true;
			boolean useDigits = true;
			final String token = RandomString.generateRandomStringBounded(length, useLetters, useDigits);

			ActivationToken activationCredentials = ActivationToken.builder().userId(userId).token(token).build();

			return UserActivationMapper.create(activationCredentials);
		}))).flatMap(tokenRepository::save);

		return this.personRepository.findById(userId).switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
				.flatMap(c -> {
					return this.tokenRepository.findByUserIdAndStatusIgnoreCase(userId, StatusType.VALID.name())
							.switchIfEmpty(Mono.empty()).map(p -> {
								return p;
							}).switchIfEmpty(alternativeMono).map(UserActivationMapper::toViewObject);
				});

	}

	@Override
	public Mono<Boolean> validateActivationToken(@NotBlank(message = "{userId.notBlank}") final String userId, 
			@NotBlank(message = "{token.notBlank}") final String token) {
		// TODO Auto-generated method stub

		return this.personRepository.findById(userId).switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
				.flatMap(c -> {
					return this.tokenRepository.findByUserIdAndStatusIgnoreCase(userId, StatusType.VALID.name())
							.switchIfEmpty(Mono.empty()).map(p -> {
								return p.getToken().equals(token);
							}).switchIfEmpty(Mono.just(Boolean.FALSE));
				});
	}

	/**
	 * Invoked by the Scheduler to expire Activation token records to mark them for
	 * removal
	 */
	@Override
	@Transactional
	@Async("threadPoolTaskExecutor")
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public void expireActivationTokenRecords() {
		try {
			// To do:
			int EXPIRATION_PERIOD = dataStore.getActivationTokenExpirationPeriod();
			log.info("ACTIVATION TOKEN EXPIRATION_PERIOD Value: {}", EXPIRATION_PERIOD);

			// LocalDateTime now = DateUtil.NOW;
			LocalDateTime lastExpiryDate = LocalDateTime.now(ZoneOffset.UTC).minusHours(EXPIRATION_PERIOD).truncatedTo(ChronoUnit.HOURS);

			log.info("Last Expiry Date {}", lastExpiryDate); 
			
			Query query = new Query();
			query.addCriteria(Criteria.where("CREATED_DTE").lte(lastExpiryDate).and("STATUS").is(StatusType.VALID.name()));
					
			Update update = new Update();

			// update age to 11
			update.set("EXPIRED_TIME", LocalDateTime.now(ZoneOffset.UTC));
			update.set("STATUS", StatusType.EXPIRED.name());

			// update all matched, both 1004 and 1005
			this.template.updateMulti(query, update, UserActivation.class).log().map(p -> p.getModifiedCount())
					.subscribe(c -> log.info("{} Activation token record(s) expired!", c));

		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException("An unexpected error occured while expiring Activation tokens",
					exp);
		}
	}

	/**
	 * Invoked by the Cleanup Scheduler to rid the database of Expired records
	 */
	@Override
	@Async("threadPoolTaskExecutor")
	@Transactional
	public void removeExpiredActivationTokenRecords() {

		try {
			// To do:
			int DELETION_PERIOD = dataStore.getActivationTokenDeletionPeriod();
			log.info("ACTIVATION TOKEN DELETION_PERIOD Value: {}", DELETION_PERIOD);

			// LocalDateTime now = DateUtil.NOW;
			LocalDateTime lastRemovalDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(DELETION_PERIOD).truncatedTo(ChronoUnit.DAYS);

			log.info("Last Removal Date {}", lastRemovalDate);
			
			Query query = new Query();
			query.addCriteria(Criteria.where("EXPIRED_TIME").lte(lastRemovalDate).and("STATUS").is(StatusType.EXPIRED.name()));
			//.andOperator(
				//	Criteria.where("EXPIRED_TIME").lte(lastRemovalDate), Criteria.where("EXPIRED_TIME").lt(nextDay)));

			this.template.remove(query, UserActivation.class).log().map(p -> p.getDeletedCount())
					.subscribe(c -> log.info("{}: activation token records deleted successfully.", c));

		} catch (RuntimeException exp) {
			exp.printStackTrace();

			throw new ApplicationDefinedRuntimeException(
					"An unexpected error occured while removing activation token records", exp);
		}
	}
}
