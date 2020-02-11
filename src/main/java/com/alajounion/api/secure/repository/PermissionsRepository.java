/**
 * 
 */
package com.alajounion.api.secure.repository;

import org.springframework.stereotype.Repository;

import com.alajounion.api.secure.domain.AppPermission;
import com.alajounion.api.secure.repository.base.BaseRepository;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface PermissionsRepository extends BaseRepository<AppPermission, String> {

	Mono<AppPermission> findByPermission(String permission);

}
