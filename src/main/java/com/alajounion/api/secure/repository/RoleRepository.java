/**
 * 
 */
package com.alajounion.api.secure.repository;

import org.springframework.stereotype.Repository;

import com.alajounion.api.secure.domain.AppRole;
import com.alajounion.api.secure.repository.base.BaseRepository;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface RoleRepository extends BaseRepository<AppRole, String> {

	Mono<AppRole> findByRoleName(String roleName);
}
