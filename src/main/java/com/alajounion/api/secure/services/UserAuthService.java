/**
 * 
 */
package com.alajounion.api.secure.services;

import java.sql.SQLException;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import com.alajounion.api.secure.domain.vo.UserVO;
import com.alajounion.api.server.models.ExpireStatusUpdateRequest;
import com.alajounion.api.server.models.LockStatusUpdateRequest;
import com.alajounion.api.server.models.PasswordUpdateRequest;
import com.cometbid.project.security.audit.Username;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
//@Service
public interface UserAuthService {
	
	Mono<Username> getCurrentUser();

	Mono<UserVO> findById(final String id);

	Mono<UserVO> save(final UserVO user);
	
	Mono<UserVO> authenticate(final String id, final String username, final String password);

	@Retryable(value = { OptimisticLockingFailureException.class, SQLException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	Mono<UserVO> changePasswd(final PasswordUpdateRequest passwdUpd);

	Mono<UserVO> findByIdAndUsername(final String id, final String username);

	// @Retryable(value = { OptimisticLockingFailureException.class, SQLException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	Mono<UserVO> lockUserStatus(final String id, final LockStatusUpdateRequest status);

	// @Retryable(value = { OptimisticLockingFailureException.class, SQLException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	Mono<UserVO> expireUserStatus(final String id, final ExpireStatusUpdateRequest status);

	// @Retryable(value = { OptimisticLockingFailureException.class, SQLException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
	void expirePasswordRecords();

	Flux<UserVO> saveUsers(final Flux<UserVO> itrUsers);

	Mono<Object> clearAllRecords();

	Mono<Map<String, Object>> findAll(final Integer pageNo, final Integer pageSize);

}
