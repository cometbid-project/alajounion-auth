/**
 * 
 */
package com.alajounion.api.secure.services;

import com.cometbid.project.common.utils.ActivationToken;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
//@Service
public interface ActivationTokenService {

	// @Transactional
	Mono<ActivationToken> generateActivationToken(final String userId);
	
	void expireActivationTokenRecords();
	
	void removeExpiredActivationTokenRecords();

	Mono<Boolean> validateActivationToken(final String userId, final String token);

}
