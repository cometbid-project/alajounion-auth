/**
 * 
 */
package com.alajounion.api.secure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
@Configuration
@PropertySource(value = "classpath:authProfile.properties")
public class AuthProfile {

	@Value("${auth.params.maximum_login_attempt}")
	private Integer maximumLoginAttempt;

	@Value("${auth.params.password_expiration}")
	private Integer passwordExpirationPeriod;

	@Value("${auth.params.failed_login_expiration}")
	private Integer failedLoginExpirationPeriod;
	
	@Value("${auth.params.success_login_expiration}")
	private Integer successLoginExpirationPeriod;

	@Value("${auth.params.success_login_deletion}")
	private Integer successLoginDeletionPeriod;

	@Value("${auth.params.failed_login_deletion}")
	private Integer failedLoginDeletion;

	@Value(value = "${auth.params.max_password_history}")
	private Integer maximumPasswordHistory;
	
	@Value(value = "${auth.params.activation_token_expiration}")
	private Integer activationTokenExpirationPeriod;
	
	@Value(value = "${auth.params.activation_token_deletion}")
	private Integer activationTokenDeletionPeriod;

}
