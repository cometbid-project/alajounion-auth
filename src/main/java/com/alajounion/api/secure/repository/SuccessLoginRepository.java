/**
 * 
 */
package com.alajounion.api.secure.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import com.alajounion.api.secure.domain.SuccessLogin;
import com.alajounion.api.secure.repository.base.BaseRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface SuccessLoginRepository extends BaseRepository<SuccessLogin, String> {

	// Done
	Flux<SuccessLogin> findByIpAddr(String ipAddr);

	Mono<Long> countByIpAddr(String ipAddr);

	// Done
	Flux<SuccessLogin> findByIpAddrAndStatusIgnoreCase(String ipAddr, String status);

	Mono<Long> countByIpAddrAndStatusIgnoreCase(String ipAddr, String status);

	// Done
	Flux<SuccessLogin> findByStatusIgnoreCase(String status);

	Mono<Long> countByStatusIgnoreCase(String status);

	// Done
	Flux<SuccessLogin> findByUsernameIgnoreCaseOrIpAddrAndStatusIgnoreCase(String username, String ipAddr, String status);

	// Done
	Mono<Long> countByUsernameIgnoreCaseOrIpAddrAndStatusIgnoreCase(String username, String ipAddr, String status);

	// Done
	Flux<SuccessLogin> findByUsernameIgnoreCaseOrIpAddr(String username, String ipAddr);

	// Done
	Mono<Long> countByUsernameIgnoreCaseOrIpAddr(String username, String ipAddr);

	// Done
	Flux<SuccessLogin> findByIdAndUsernameIgnoreCase(String id, String username);

	// Done
	Mono<Long> countByIdAndUsernameIgnoreCase(String id, String username);

	// Done
	Flux<SuccessLogin> findByIdAndUsernameIgnoreCaseAndStatusIgnoreCase(String id, String username, String status);

	// Done
	Mono<Long> countByIdAndUsernameIgnoreCaseAndStatusIgnoreCase(String id, String username, String status);

	// Done
	Flux<SuccessLogin> findByUsernameIgnoreCaseAndIpAddr(String username, String ipAddr);

	// Done
	Mono<Long> countByUsernameIgnoreCaseAndIpAddr(String username, String ipAddr);

	// Done
	Flux<SuccessLogin> findByUsernameIgnoreCaseAndIpAddrAndStatusIgnoreCase(String username, String ipAddr, String status);

	// Done
	Mono<Long> countByUsernameIgnoreCaseAndIpAddrAndStatusIgnoreCase(String username, String ipAddr, String status);

	// @Query(value = "SELECT b FROM SuccessLogin b WHERE b.loginTime BETWEEN
	// :startTime AND :endTime ORDER BY b.loginTime")
	Flux<SuccessLogin> findByLoginTimeBetweenOrderByLoginTime(LocalDate startTime, LocalDate endTime);

	// @CountQuery(value = "SELECT count(b) FROM SuccessLogin b WHERE b.loginTime
	// BETWEEN :startTime AND :endTime ORDER BY b.loginTime")
	Mono<Long> countByLoginTimeBetweenOrderByLoginTime(LocalDateTime startTime, LocalDateTime endTime);

	Mono<Long> countByLoginTimeBetweenOrderByLoginTime(LocalDate startDate,
			 LocalDate endDate);

	// Mono<Map<String, Object>> findByLoginTimeBetweenOrderByLoginTime(LocalDate startDate, LocalDate endDate);

}
