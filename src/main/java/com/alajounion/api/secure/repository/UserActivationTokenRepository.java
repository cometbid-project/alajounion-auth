/**
 * 
 */
package com.alajounion.api.secure.repository;

import com.alajounion.api.secure.domain.UserActivation;
import com.alajounion.api.secure.repository.base.BaseRepository;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface UserActivationTokenRepository extends BaseRepository<UserActivation, String> {

	Mono<UserActivation> findByUserId(String userId);
	
	Mono<UserActivation> findByUserIdAndStatusIgnoreCase(String userId, String status);

}
