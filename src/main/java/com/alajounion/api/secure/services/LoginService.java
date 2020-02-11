/**
 * 
 */
package com.alajounion.api.secure.services;

import java.time.LocalDate;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import com.alajounion.api.secure.domain.vo.FailedLoginVO;
import com.alajounion.api.secure.domain.vo.SuccessLoginVO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
//@Service
public interface LoginService {

	Mono<Map<String, Object>> findAllSuccessLogin(final Integer pageNo, final Integer pageSize);

	Mono<Map<String, Object>> findAllFailedLogin(final Integer pageNo, final Integer pageSize);

	Mono<Map<String, Object>> findAllSuccessLoginByStatus(final String status, final Integer pageNo,
			final Integer pageSize);

	Mono<Map<String, Object>> findAllFailedLoginByStatus(final String status, final Integer pageNo,
			final Integer pageSize);

	Mono<Map<String, Object>> findSuccessLoginByIP(final String ipAddress, final String status, final Integer pageNo,
			final Integer pageSize);

	Mono<Map<String, Object>> findFailedLoginByIP(final String ipAddress, final String status, final Integer pageNo,
			final Integer pageSize);

	Mono<Map<String, Object>> findSuccessLoginByIPAndUsername(final String ipAddress, final String username,
			final String status, final Integer pageNo, final Integer pageSize);

	Mono<Map<String, Object>> findSuccessLoginByIPOrUsername(final String ipAddress, final String username,
			final String status, final Integer pageNo, final Integer pageSize);

	Mono<Map<String, Object>> findSuccessLoginByIdAndUsername(final String id, final String username,
			final String status, final Integer pageNo, final Integer pageSize);

	// @Retryable(value = { OptimisticLockingFailureException.class }, maxAttempts =
	// 3, backoff = @Backoff(delay = 100))
	Flux<String> invalidateUserSessions(final String id, final String username);

	Mono<SuccessLoginVO> recordSuccessLogin(final SuccessLoginVO loginVo);

	Mono<Map<String, Object>> findSuccessfulLoginsBetweenDate(final Integer pageNo, final Integer pageSize,
			final LocalDate startDate, final LocalDate endDate);

	Mono<Map<String, Object>> findFailedLoginsBetweenDate(final Integer pageNo, final Integer pageSize,
			final LocalDate startDate, final LocalDate endDate);

	// @Retryable(value = { OptimisticLockingFailureException.class }, maxAttempts =
	// 3, backoff = @Backoff(delay = 100))
	Flux<FailedLoginVO> incrementFailedLogins(final String ipAddress);

	// @Retryable(value = { OptimisticLockingFailureException.class }, maxAttempts =
	// 3, backoff = @Backoff(delay = 1000))
	void expireFailedLoginRecords();

	// @Retryable(value = { OptimisticLockingFailureException.class }, maxAttempts =
	// 3, backoff = @Backoff(delay = 1000))
	public void expireSuccessLoginRecords();

	void removeExpiredLoginRecords();

	Mono<Object> clearAllSuccessRecords();
}
