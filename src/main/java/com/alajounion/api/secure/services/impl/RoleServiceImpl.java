/**
 * 
 */
package com.alajounion.api.secure.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.alajounion.api.secure.domain.AppPermission;
import com.alajounion.api.secure.domain.AppRole;
import com.alajounion.api.secure.repository.PermissionsRepository;
import com.alajounion.api.secure.repository.RoleRepository;
import com.alajounion.api.secure.services.RoleService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Service
@Validated
public class RoleServiceImpl implements RoleService {

	@Autowired
	private PermissionsRepository permRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public Flux<AppRole> findAllRoles() {
		return roleRepository.findAll();
	}

	@Override
	public Flux<AppPermission> findAllPermissions() {
		return permRepository.findAll();
	}

	@Override
	@Transactional
	public Flux<AppRole> saveRoles(final Flux<AppRole> itrRoles) {
		return roleRepository.saveAll(itrRoles);
	}

	@Override
	@Transactional
	public Mono<AppRole> saveRole(final AppRole singleRole) {
		// TODO Auto-generated method stub
		return Mono.just(singleRole).flatMap(roleRepository::save);
	}

}
