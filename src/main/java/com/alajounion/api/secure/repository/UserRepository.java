/**
 * 
 */
package com.alajounion.api.secure.repository;

import org.springframework.stereotype.Repository;

import com.alajounion.api.secure.domain.UserAuth;
import com.alajounion.api.secure.repository.base.BaseRepository;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends BaseRepository<UserAuth, String> {

	Mono<UserAuth> findByIdAndUsernameIgnoreCase(String id, String username);

}