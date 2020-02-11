/**
 * 
 */
package com.alajounion.api.secure.services.impl;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alajounion.api.secure.services.ActivationTokenService;
import com.alajounion.api.secure.services.LoginService;
import com.alajounion.api.secure.services.UserAuthService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Gbenga
 *
 */
@Slf4j
@Component("cleanupScheduledAnnotationService")
public class AuthScheduler {

	@Autowired
	private UserAuthService passwdService;

	@Autowired
	private LoginService loginService;

	@Autowired
	private ActivationTokenService tokenService;

	@Scheduled(cron = "${hourly.cron.expression}")
	public void hourlyJob() throws InterruptedException {

		log.info("Scheduler to update failed logins Starts...{}", Instant.now());
		this.loginService.expireFailedLoginRecords();

		log.info("Scheduler to update success logins Starts...{}", Instant.now());
		this.loginService.expireSuccessLoginRecords();

		log.info("Scheduler to update expired activation token records Starts...{}", Instant.now());
		this.tokenService.expireActivationTokenRecords();

	}

	@Scheduled(cron = "${daily.cron.expression}")
	public void dailyJob() throws InterruptedException {

		log.info("Scheduler to change password status Starts...{}", Instant.now());
		this.passwdService.expirePasswordRecords();

		log.info("Scheduler to remove expired login records Starts...{}", Instant.now());
		this.loginService.removeExpiredLoginRecords();

		log.info("Scheduler to remove expired login records Starts...{}", Instant.now());
		this.tokenService.removeExpiredActivationTokenRecords();
	}

}
