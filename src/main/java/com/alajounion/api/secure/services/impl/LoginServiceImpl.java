/**
 * 
 */
package com.alajounion.api.secure.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.alajounion.api.secure.domain.FailedLogin;
import com.alajounion.api.secure.domain.SuccessLogin;
import com.alajounion.api.secure.domain.mappers.FailedLoginMapper;
import com.alajounion.api.secure.domain.mappers.SuccessLoginMapper;
import com.alajounion.api.secure.domain.vo.FailedLoginVO;
import com.alajounion.api.secure.domain.vo.SuccessLoginVO;
import com.alajounion.api.secure.embeddables.UsrLoginLoc;
import com.alajounion.api.secure.repository.FailedLoginRepository;
import com.alajounion.api.secure.repository.SuccessLoginRepository;
import com.alajounion.api.secure.services.LoginService;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.exceptions.ApplicationDefinedRuntimeException;
import com.cometbid.project.common.exceptions.InvalidParameterException;
import com.cometbid.project.common.exceptions.MaximumLoginAttemptReachedException;
import com.cometbid.project.common.utils.DateUtil;
import com.cometbid.project.common.utils.PaginationHelper;
import com.cometbid.project.common.validators.qualifiers.ConsistentDateParameters;
import com.cometbid.project.common.validators.qualifiers.PagingValidator;
import com.cometbid.project.common.validators.qualifiers.ValidDate;
import com.vladmihalcea.concurrent.Retry;
import com.alajounion.api.secure.config.AuthProfile;
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
@Service
@Validated
public class LoginServiceImpl implements LoginService {

	@Autowired
	ReactiveMongoTemplate template;

	@Autowired
	private AuthProfile dataStore;

	private SuccessLoginRepository successLoginRepository;
	private FailedLoginRepository failedLoginRepository;

	public LoginServiceImpl(SuccessLoginRepository successLoginRepository,
			FailedLoginRepository failedLoginRepository) {
		this.successLoginRepository = successLoginRepository;
		this.failedLoginRepository = failedLoginRepository;
	}

	/**
	 * Search all Successful login records with pagination enabled
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found
	 */
	@Override
	public Mono<Map<String, Object>> findAllSuccessLogin(
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			final Map<String, Object> map = new HashMap<>();

			return successLoginRepository.count().log().map(Count::new).flatMap(c -> {
				log.info("{} Successful login record(s) found!", c.getCount());
				map.put("recordCount", c.getCount());

				int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

				Flux<SuccessLoginVO> loginVo = this.successLoginRepository.findAll().skip(adjPageNo * pageSize)
						.take(pageSize).map(SuccessLoginMapper::toViewObject).switchIfEmpty(Mono.empty());

				map.put("result", loginVo);

				return Mono.just(map);
			});

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving all Successful login records", ex)
							.addContextValue("Page No", pageNo).addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search all Failed login records with pagination enabled
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found
	 */
	@Override
	public Mono<Map<String, Object>> findAllFailedLogin(
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}")final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			final Map<String, Object> map = new HashMap<>();

			return failedLoginRepository.count().log().map(Count::new).flatMap(c -> {
				log.info("{} Failed login record(s) found!", c.getCount());
				map.put("recordCount", c.getCount());

				int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

				Flux<FailedLoginVO> loginVo = this.failedLoginRepository.findAll().skip(adjPageNo * pageSize)
						.take(pageSize).map(FailedLoginMapper::toViewObject).switchIfEmpty(Mono.empty());

				map.put("result", loginVo);

				return Mono.just(map);
			});

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving all Failed login records", ex)
							.addContextValue("Page No", pageNo).addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search by status, successful login records with pagination enabled
	 * 
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found
	 */
	@Override
	public Mono<Map<String, Object>> findAllSuccessLoginByStatus(@NotBlank final String status,
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			final Map<String, Object> map = new HashMap<>();

			return successLoginRepository.countByStatusIgnoreCase(status).log().map(Count::new).flatMap(c -> {
				log.info("{} Successful login record(s) found by Status!", c.getCount());
				map.put("recordCount", c.getCount());

				int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

				Flux<SuccessLoginVO> loginVo = this.successLoginRepository.findByStatusIgnoreCase(status)
						.skip(adjPageNo * pageSize).take(pageSize).map(SuccessLoginMapper::toViewObject);

				map.put("result", loginVo);

				return Mono.just(map);
			});

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving Successful login record by Status", ex)
							.addContextValue("Status ", status).addContextValue("Page No", pageNo)
							.addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search by status, failed login records with pagination enabled
	 * 
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found
	 */
	@Override
	public Mono<Map<String, Object>> findAllFailedLoginByStatus(@NotBlank final String status,
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			final Map<String, Object> map = new HashMap<>();

			return failedLoginRepository.countByStatus(status).log().map(Count::new).flatMap(c -> {
				log.info("{} Failed login record(s) found by Status!", c.getCount());
				map.put("recordCount", c.getCount());

				int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

				Flux<FailedLoginVO> loginVo = this.failedLoginRepository.findByStatusIgnoreCase(status)
						.skip(adjPageNo * pageSize).take(pageSize).map(FailedLoginMapper::toViewObject);

				map.put("result", loginVo);

				return Mono.just(map);
			});

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving Failed login record by Status", ex)
							.addContextValue("Status ", status).addContextValue("Page No", pageNo)
							.addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search by ip address and status, successful login records with pagination
	 * enabled
	 * 
	 * @param ipAddress
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found. Use for all kinds
	 *         of search with criteria status(EXPIRED, LOCKED, VALID/ACTIVE). To
	 *         search irrespective of status set argument to null
	 * 
	 */
	@Override
	public Mono<Map<String, Object>> findSuccessLoginByIP(@NotBlank final String ipAddress, final String status,
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			final Map<String, Object> map = new HashMap<>();
			final int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

			if (status == null) {
				return successLoginRepository.countByIpAddr(ipAddress).log().map(Count::new).flatMap(c -> {
					log.info("{} Successful login record(s) found by IP and Status!", c.getCount());
					map.put("recordCount", c.getCount());

					Flux<SuccessLoginVO> records = this.successLoginRepository.findByIpAddr(ipAddress)
							.skip(adjPageNo * pageSize).take(pageSize).map(SuccessLoginMapper::toViewObject);

					map.put("result", records);

					return Mono.just(map);
				});
			} else {
				return successLoginRepository.countByIpAddrAndStatusIgnoreCase(ipAddress, status).log().map(Count::new)
						.flatMap(c -> {
							log.info("{} Successful login record(s) found by IP and Status!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<SuccessLoginVO> records = this.successLoginRepository
									.findByIpAddrAndStatusIgnoreCase(ipAddress, status).skip(adjPageNo * pageSize)
									.take(pageSize).map(SuccessLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			}

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving Successful login record by IP Address", ex)
							.addContextValue("IP Address ", ipAddress).addContextValue("Status ", status)
							.addContextValue("Page No", pageNo).addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search by ip address and status, failed login records with pagination enabled
	 * 
	 * @param ipAddress
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found. Use for all kinds
	 *         of search with criteria status(EXPIRED, LOCKED, VALID/ACTIVE). To
	 *         search irrespective of status set argument to null
	 * 
	 */
	@Override
	public Mono<Map<String, Object>> findFailedLoginByIP(@NotBlank final String ipAddress, final String status,
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			final Map<String, Object> map = new HashMap<>();
			final int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

			if (status == null) {
				return failedLoginRepository.countByIpAddr(ipAddress).log().map(Count::new).flatMap(c -> {
					log.info("{} Failed login record(s) found by IP and Status!", c.getCount());
					map.put("recordCount", c.getCount());

					Flux<FailedLoginVO> records = this.failedLoginRepository.findByIpAddr(ipAddress)
							.skip(adjPageNo * pageSize).take(pageSize).map(FailedLoginMapper::toViewObject);

					map.put("result", records);

					return Mono.just(map);
				});
			} else {
				return failedLoginRepository.countByIpAddrAndStatusIgnoreCase(ipAddress, status).log().map(Count::new)
						.flatMap(c -> {
							log.info("{} Failed login record(s) found by IP and Status!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<FailedLoginVO> records = this.failedLoginRepository
									.findByIpAddrAndStatusIgnoreCase(ipAddress, status).skip(adjPageNo * pageSize)
									.take(pageSize).map(FailedLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			}

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving Failed login record by IP Address", ex)
							.addContextValue("IP Address ", ipAddress).addContextValue("Status ", status)
							.addContextValue("Page No", pageNo).addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search by username and ip address and status, successful login records with
	 * pagination enabled
	 * 
	 * @param ipAddress
	 * @param username
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found. Use for all kinds
	 *         of search with criteria status(EXPIRED, LOCKED, VALID/ACTIVE). To
	 *         search irrespective of status set argument to null
	 */
	@Override
	public Mono<Map<String, Object>> findSuccessLoginByIPAndUsername(@NotBlank final String ipAddress,
			@NotBlank final String username, final String status,
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			// Adjust to ensure first page doesn't skip any record
			final Map<String, Object> map = new HashMap<>();
			final int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

			if (status == null) {
				return successLoginRepository.countByUsernameIgnoreCaseAndIpAddr(username, ipAddress).log()
						.map(Count::new).flatMap(c -> {
							log.info("{} Successful login record(s) by username and IP Address found!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<SuccessLoginVO> records = successLoginRepository
									.findByUsernameIgnoreCaseAndIpAddr(username, ipAddress).skip(adjPageNo * pageSize)
									.take(pageSize).map(SuccessLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			} else {
				return successLoginRepository
						.countByUsernameIgnoreCaseAndIpAddrAndStatusIgnoreCase(username, ipAddress, status).log()
						.map(Count::new).flatMap(c -> {
							log.info("{} Successful login record(s) by username and IP Address found!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<SuccessLoginVO> records = this.successLoginRepository
									.findByUsernameIgnoreCaseAndIpAddrAndStatusIgnoreCase(username, ipAddress, status)
									.skip(adjPageNo * pageSize).take(pageSize).map(SuccessLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			}

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving Success login record by IP Address and username", ex)
							.addContextValue("IP Address ", ipAddress).addContextValue("Username", username)
							.addContextValue("Status ", status).addContextValue("Page No", pageNo)
							.addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search by username or ip address, and status successful login records with
	 * pagination enabled. Either parameter here can be null but not both
	 * 
	 * @param ipAddress
	 * @param username
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found. Use for all kinds
	 *         of search with criteria status(EXPIRED, LOCKED, VALID/ACTIVE) by
	 *         username and ip address. To search irrespective of status set
	 *         argument to null
	 */
	@Override
	public Mono<Map<String, Object>> findSuccessLoginByIPOrUsername(final String ipAddress, 
			final String username, final String status,
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		if (ipAddress == null && username == null) {
			throw new InvalidParameterException("At least one parameter must be specified");
		}

		try {
			// Adjust to ensure first page doesn't skip any record
			final Map<String, Object> map = new HashMap<>();
			final int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

			if (status == null) {
				return successLoginRepository.countByUsernameIgnoreCaseOrIpAddr(username, ipAddress).log()
						.map(Count::new).flatMap(c -> {
							log.info("{} Successful login record(s) by username and IP Address found!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<SuccessLoginVO> records = this.successLoginRepository
									.findByUsernameIgnoreCaseOrIpAddr(username, ipAddress).skip(adjPageNo * pageSize)
									.take(pageSize).map(SuccessLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			} else {
				return successLoginRepository
						.countByUsernameIgnoreCaseOrIpAddrAndStatusIgnoreCase(username, ipAddress, status).log()
						.map(Count::new).flatMap(c -> {
							log.info("{} Successful login record(s) by username and IP Address found!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<SuccessLoginVO> records = this.successLoginRepository
									.findByUsernameIgnoreCaseOrIpAddrAndStatusIgnoreCase(username, ipAddress, status)
									.skip(adjPageNo * pageSize).take(pageSize).map(SuccessLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			}

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving Success login record by IP Address and/or username", ex)
							.addContextValue("IP Address ", ipAddress).addContextValue("Username", username)
							.addContextValue("Status ", status).addContextValue("Page No", pageNo)
							.addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Search by id and username, and status successful login records with
	 * pagination enabled
	 * 
	 * @param id
	 * @param username
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found. Use for all kinds
	 *         of search with criteria status(EXPIRED, LOCKED, VALID/ACTIVE) by id
	 *         and username. To search irrespective of status set argument to null
	 */
	@Override
	public Mono<Map<String, Object>> findSuccessLoginByIdAndUsername(@NotBlank final String id, 
			@NotBlank final String username,
			final String status,
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize) {

		try {
			// Adjust to ensure first page doesn't skip any record
			final Map<String, Object> map = new HashMap<>();
			final int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

			if (status == null) {
				return successLoginRepository.countByIdAndUsernameIgnoreCase(id, username).log().map(Count::new)
						.flatMap(c -> {
							log.info("{} Successful login record(s) by username and id Address found!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<SuccessLoginVO> records = this.successLoginRepository
									.findByIdAndUsernameIgnoreCase(id, username).skip(adjPageNo * pageSize)
									.take(pageSize).map(SuccessLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			} else {
				return successLoginRepository.countByIdAndUsernameIgnoreCaseAndStatusIgnoreCase(id, username, status)
						.log().map(Count::new).flatMap(c -> {
							log.info("{} Successful login record(s) by username and id Address found!", c.getCount());
							map.put("recordCount", c.getCount());

							Flux<SuccessLoginVO> records = this.successLoginRepository
									.findByIdAndUsernameIgnoreCaseAndStatusIgnoreCase(id, username, status)
									.skip(adjPageNo * pageSize).take(pageSize).map(SuccessLoginMapper::toViewObject);

							map.put("result", records);

							return Mono.just(map);
						});
			}

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving Success login record by Id and username", ex)
							.addContextValue("Id ", id).addContextValue("Username", username)
							.addContextValue("Status ", status).addContextValue("Page No", pageNo)
							.addContextValue("Page size", pageSize);
		}
	}

	/**
	 * Expires User successful login record signaling a logout
	 * 
	 * @param id
	 * @param username
	 * @return Mono<Void>
	 */
	@Override
	@Transactional
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public Flux<String> invalidateUserSessions(@NotBlank final String id, @NotBlank final String username) {

		log.info("Invalidate Session ....");
		log.info("Parameter {}  {} ....", id, username);

		try {
			return this.successLoginRepository
					.findByIdAndUsernameIgnoreCaseAndStatusIgnoreCase(id, username, StatusType.VALID.name())
					.switchIfEmpty(Flux.empty()).map(p -> {
						p.setStatus(StatusType.EXPIRED.name());
						p.setLogoutTime(LocalDateTime.now(ZoneOffset.UTC));
						return p;
					}).flatMap(successLoginRepository::save).switchIfEmpty(Flux.empty())
					.flatMap(c -> Flux.just("Session logout completed")).log("Session logout completed");

		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException(
					"An unexpected error occured while invalidating User's past sessions", exp)
							.addContextValue("Username: ", username).addContextValue("Id: ", id);
		}

	}

	/**
	 * Create a record to indicate Successful login by a User
	 * 
	 * @param loginVo
	 * @return Mono<SuccessLoginVO> Successful login record created with id
	 */
	@Override
	@Transactional
	public Mono<SuccessLoginVO> recordSuccessLogin(@NonNull @Valid final SuccessLoginVO loginVo) {

		log.info("Recording Successful Login...");
		try {

			SuccessLogin successLogin = SuccessLoginMapper.create(loginVo);
			captureLoginLocation(successLogin, loginVo.getLoginLocHis());

			return successLoginRepository.insert(successLogin).map(SuccessLoginMapper::toViewObject);

		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException(
					"An unexpected error occured while recording successful logins", exp);
		}
	}

	/**
	 * Search by login time successful login records with pagination enabled
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param startDate
	 * @param endDate
	 * @param sortOrders
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found.
	 */
	@Override
	@ConsistentDateParameters
	public Mono<Map<String, Object>> findSuccessfulLoginsBetweenDate(
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize,
			@NonNull @ValidDate final LocalDate startDate, @NonNull @ValidDate final LocalDate endDate) {

		log.info("Business Logic Start Date: {}", startDate);
		log.info("Business Logic End Date: {}", endDate);

		try {

			// Adjust to ensure first page doesn't skip any record
			final Map<String, Object> map = new HashMap<>();
			final int adjPageNo = PaginationHelper.adjustPageNo(pageNo);
			return successLoginRepository.countByLoginTimeBetweenOrderByLoginTime(startDate, endDate).log()
					.map(Count::new).flatMap(c -> {
						log.info("{} Successful login record(s) found!", c.getCount());
						map.put("recordCount", c.getCount());

						Flux<SuccessLoginVO> records = successLoginRepository
								.findByLoginTimeBetweenOrderByLoginTime(startDate, endDate).skip(adjPageNo * pageSize)
								.take(pageSize).map(SuccessLoginMapper::toViewObject);

						map.put("result", records);

						return Mono.just(map);
					});

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving successful login records between dates", ex)
							.addContextValue("Start Date", startDate).addContextValue("End Date", endDate)
							.addContextValue("Page No", pageNo).addContextValue("Page size", pageSize);
		}

	}

	/**
	 * Search by last time failed login records with pagination enabled
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param startDate
	 * @param endDate
	 * @return Mono<Map<String, Object>>
	 * 
	 *         A map containing count and list of records found.
	 */
	@Override
	@ConsistentDateParameters
	public Mono<Map<String, Object>> findFailedLoginsBetweenDate(
			@NotNull(message = "{PageNo.notNull}") @PagingValidator(message = "{PageNo.paging.start}") final Integer pageNo,
			@NotNull(message = "{PageSize.notNull}") @PagingValidator(message = "{PageSize.paging.size}") final Integer pageSize,
			@NonNull @ValidDate final LocalDate startDate, @NonNull @ValidDate final LocalDate endDate) {

		log.info("Business Logic Start Date: {}", startDate);
		log.info("Business Logic End Date: {}", endDate);

		try {
			final Map<String, Object> map = new HashMap<>();
			final int adjPageNo = PaginationHelper.adjustPageNo(pageNo);

			return failedLoginRepository.countByLastTimeBetweenOrderByLastTime(startDate, endDate).log().map(Count::new)
					.flatMap(c -> {
						log.info("{} Failed login record(s) found!", c.getCount());
						map.put("recordCount", c.getCount());

						Flux<FailedLoginVO> records = failedLoginRepository
								.findByLastTimeBetweenOrderByLastTime(startDate, endDate).skip(adjPageNo * pageSize)
								.take(pageSize).map(FailedLoginMapper::toViewObject);

						map.put("result", records);

						return Mono.just(map);
					});

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while retrieving failed login records between dates", ex)
							.addContextValue("Start Date", startDate).addContextValue("End Date", endDate)
							.addContextValue("Page No", pageNo).addContextValue("Page size", pageSize);
		}

	}

	/**
	 * Signal failed login attempts to avoid Resource denial attack. As soon as
	 * maximum attempt is reached throws a RuntimeException
	 * 
	 * @param ipAddress
	 * @return Mono<Void>
	 *
	 */
	@Override
	@Transactional
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public Flux<FailedLoginVO> incrementFailedLogins(@NotBlank final String ipAddress) {

		try {

			Mono<FailedLogin> alternativeMono = Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
				FailedLoginVO failedLoginVo = FailedLoginVO.builder().ipAddr(ipAddress).build();

				return FailedLoginMapper.create(failedLoginVo);
			})));

			return failedLoginRepository.findByIpAddrAndStatusIgnoreCase(ipAddress, StatusType.VALID.name())
					.switchIfEmpty(Mono.empty()).map(record -> {
						log.info("Failed login record(s) found! {} ", record);

						int attempts = record.getLoginAttempt() + 1;

						// To do:
						final int MAXIMUM_VAL = dataStore.getMaximumLoginAttempt();
						if (attempts <= MAXIMUM_VAL) {
							record.setLastTime(DateUtil.NOW);
							record.setLoginAttempt(attempts);
							return record;
						} else {
							throw new MaximumLoginAttemptReachedException("Maximum login attempt reached");
						}

					}).switchIfEmpty(alternativeMono).flatMap(failedLoginRepository::save)
					.map(FailedLoginMapper::toViewObject);					

		} catch (RuntimeException ex) {
			throw new ApplicationDefinedRuntimeException(
					"Unexpected error occured while incrementing failed login attempt record", ex)
							.addContextValue("IP Address", ipAddress);
		}
	}

	/**
	 * Invoked by the Scheduler to expire Failed login records to mark them for
	 * removal
	 */
	@Override
	@Transactional
	@Async("threadPoolTaskExecutor")
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public void expireFailedLoginRecords() {
		try {
			// To do:
			final long EXPIRATION_PERIOD = dataStore.getFailedLoginExpirationPeriod();
			log.info("EXPIRATION PERIOD Value for failed logins: {}", EXPIRATION_PERIOD);

			LocalDateTime lastExpiryDate = LocalDateTime.now(ZoneOffset.UTC).minusHours(EXPIRATION_PERIOD)
					.truncatedTo(ChronoUnit.HOURS);

			Query query = new Query();
			query.addCriteria(
					Criteria.where("LAST_TIME").lte(lastExpiryDate).and("STATUS").is(StatusType.VALID.name()));
			// .andOperator(
			// Criteria.where("LAST_TIME").gte(lastExpiryDate),
			// Criteria.where("LAST_TIME").lt(nextHour)));

			Update update = new Update();

			// update age to 11
			update.set("EXPIRED_TIME", LocalDateTime.now(ZoneOffset.UTC));
			update.set("STATUS", StatusType.EXPIRED.name());

			// update all matched, both 1004 and 1005
			this.template.updateMulti(query, update, FailedLogin.class).log().map(p -> p.getModifiedCount())
					.subscribe(c -> log.info("{} Failed login record(s) expired!", c));

			// log.info("{}: failed logins updated successfully.", countUpdated);

		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException("An unexpected error occured while expiring failed logins",
					exp);
		}
	}

	/**
	 * Invoked by the Scheduler to expire Failed login records to mark them for
	 * removal
	 */
	@Override
	@Transactional
	@Async("threadPoolTaskExecutor")
	@Retry(times = 3, on = org.springframework.dao.OptimisticLockingFailureException.class)
	public void expireSuccessLoginRecords() {
		try {
			// To do:
			final long EXPIRATION_PERIOD = dataStore.getSuccessLoginExpirationPeriod();
			log.info("EXPIRATION PERIOD Value for successful logins: {}", EXPIRATION_PERIOD);

			LocalDateTime lastExpiryDate = LocalDateTime.now(ZoneOffset.UTC).minusHours(EXPIRATION_PERIOD)
					.truncatedTo(ChronoUnit.HOURS);

			Query query = new Query();
			query.addCriteria(
					Criteria.where("LOGIN_TIME").lte(lastExpiryDate).and("STATUS").is(StatusType.VALID.name()));

			Update update = new Update();

			// update age to 11
			update.set("EXPIRED_TIME", LocalDateTime.now(ZoneOffset.UTC));
			update.set("STATUS", StatusType.EXPIRED.name());

			// update all matched, both 1004 and 1005
			this.template.updateMulti(query, update, SuccessLogin.class).log().map(p -> p.getModifiedCount())
					.subscribe(c -> log.info("{} Success login record(s) expired!", c));

		} catch (RuntimeException exp) {
			throw new ApplicationDefinedRuntimeException("An unexpected error occured while expiring successful logins",
					exp);
		}
	}

	/**
	 * Invoked by the Cleanup Scheduler to rid the database of Expired records
	 */
	@Override
	@Async("threadPoolTaskExecutor")
	@Transactional
	public void removeExpiredLoginRecords() {

		try {
			// To do:
			int DELETION_PERIOD = dataStore.getFailedLoginDeletion();
			log.info("FAILED LOGIN DELETION_PERIOD Value: {}", DELETION_PERIOD);

			LocalDateTime lastRemovalDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(DELETION_PERIOD)
					.truncatedTo(ChronoUnit.DAYS);

			Query query = new Query();
			query.addCriteria(
					Criteria.where("EXPIRED_TIME").lte(lastRemovalDate).and("STATUS").is(StatusType.EXPIRED.name()));
			// .andOperator(
			// Criteria.where("EXPIRED_TIME").gte(lastRemovalDate),
			// Criteria.where("EXPIRED_TIME").lt(nextDay)));

			this.template.remove(query, FailedLogin.class).log().map(p -> p.getDeletedCount())
					.subscribe(c -> log.info("{}: failed login records deleted successfully.", c));

			// log.info("{}: failed login records deleted successfully.", countDeleted);
			// ================================================================================================

			DELETION_PERIOD = dataStore.getSuccessLoginDeletionPeriod();
			log.info("SUCCESS LOGIN DELETION_PERIOD Value: {}", DELETION_PERIOD);

			lastRemovalDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(DELETION_PERIOD).truncatedTo(ChronoUnit.DAYS);

			query.addCriteria(
					Criteria.where("EXPIRED_TIME").lte(lastRemovalDate).and("STATUS").is(StatusType.EXPIRED.name()));
			// .andOperator(
			// Criteria.where("EXPIRED_TIME").lte(lastRemovalDate),
			// Criteria.where("EXPIRED_TIME").lt(nextDay)));

			this.template.remove(query, SuccessLogin.class).log().map(p -> p.getDeletedCount())
					.subscribe(c -> log.info("{}: success login records deleted successfully.", c));

		} catch (RuntimeException exp) {
			exp.printStackTrace();

			throw new ApplicationDefinedRuntimeException(
					"An unexpected error occured while removing success/failed login records", exp);
		}

	}

	private void captureLoginLocation(SuccessLogin loginRecord, UsrLoginLoc userlocation) {

		if (userlocation != null) {
			loginRecord.addToLoginLocHis(userlocation);
		}
	}

	/**
	 * 
	 * @return
	 */
	public Mono<Object> clearAllSuccessRecords() {
		return this.successLoginRepository.count().flatMap(c -> {
			if (c != 0L) {
				return successLoginRepository.deleteAll().map(p -> {
					return Mono.just("Successful");
				});
			}
			return Mono.just("No record found to delete");
		});
	}

	/**
	 * 
	 * @return
	 */
	public Mono<Object> clearAllFailedRecords() {
		return this.failedLoginRepository.count().flatMap(c -> {
			if (c != 0L) {
				return failedLoginRepository.deleteAll().map(p -> {
					return Mono.just("Successful");
				});
			}
			return Mono.just("No record found to delete");
		});
	}
}
