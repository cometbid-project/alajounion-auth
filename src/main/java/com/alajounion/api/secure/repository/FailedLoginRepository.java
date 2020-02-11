/**
 * 
 */
package com.alajounion.api.secure.repository;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import com.alajounion.api.secure.domain.FailedLogin;
import com.alajounion.api.secure.repository.base.BaseRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface FailedLoginRepository extends BaseRepository<FailedLogin, String> {

	// Done
	Flux<FailedLogin> findByIpAddr(String ipAddr);

	Mono<Long> countByIpAddr(String ipAddr);

	// Done
	Flux<FailedLogin> findByIpAddrAndStatusIgnoreCase(String ipAddr, String status);

	Mono<Long> countByIpAddrAndStatusIgnoreCase(String ipAddr, String status);

	// Done
	// Flux<FailedLogin> findByStatus(String status);

	Mono<Long> countByStatus(String status);

	// @Query(value = "SELECT b FROM FailedLogin b WHERE b.loginTime BETWEEN
	// :startTime AND :endTime ORDER BY b.loginTime")
	Flux<FailedLogin> findByLastTimeBetweenOrderByLastTime(LocalDate start, LocalDate endTime);

	Flux<FailedLogin> findByStatusIgnoreCase(String status);

	// @CountQuery(value = "SELECT count(b) FROM SuccessLogin b WHERE b.loginTime
	// BETWEEN :startTime AND :endTime ORDER BY b.loginTime")
	Mono<Long> countByLastTimeBetweenOrderByLastTime(LocalDate startTime, LocalDate endTime);

}
