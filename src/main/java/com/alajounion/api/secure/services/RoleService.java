/**
 * 
 */
package com.alajounion.api.secure.services;


import com.alajounion.api.secure.domain.AppPermission;
import com.alajounion.api.secure.domain.AppRole;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
//@Service
public interface RoleService {

	// @Transactional
	Flux<AppRole> findAllRoles();

	Flux<AppPermission> findAllPermissions();

	Flux<AppRole> saveRoles(final Flux<AppRole> itrRoles);
	
	Mono<AppRole> saveRole(final AppRole singleRole);
}
