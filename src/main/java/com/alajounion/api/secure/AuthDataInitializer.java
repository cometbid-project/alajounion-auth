/**
 * 
 */
package com.alajounion.api.secure;

import org.springframework.stereotype.Component;

import com.alajounion.api.secure.domain.vo.SuccessLoginVO;
import com.alajounion.api.secure.domain.vo.UserVO;
import com.alajounion.api.secure.embeddables.UsrLoginLoc;
import com.alajounion.api.secure.rest.handler.AuthHandler;
import com.cometbid.project.common.enums.Role;
import com.alajounion.api.secure.services.ActivationTokenService;
import com.alajounion.api.secure.services.LoginService;
import com.alajounion.api.secure.services.UserAuthService;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.security.CommonSecurity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * @author Gbenga
 *
 */
@Slf4j
@Component
@Profile("demo")
public class AuthDataInitializer implements ApplicationListener<ApplicationReadyEvent> {
	
	@Autowired
	private UserAuthService userService;

	@Autowired
	private LoginService loginService;

	@Autowired
	private ActivationTokenService tokenService;

	@Autowired
	private CommonSecurity utils;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		// TODO Auto-generated method stub
		log.info("Initializing program data...");

		initializeUserData();

		initializeSuccessLoginData();

		initializeFailedLoginData();
	}
	
	private void initializeUserData() {
		log.info("Encoder used {}", utils.getEncoder().getClass().getName());

		userService.clearAllRecords().subscribe(p -> log.info("Cleared all previous records..."));
		
		UserVO[] defaultUsers = { UserVO.builder().username("test1").password("test1@cometbid.com") // test1@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ADMIN")).collect(Collectors.toSet()))
				.build(),
				UserVO.builder().username("test2").password("test2@cometbid.com") // test2@cometbid.com
						.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
								.collect(Collectors.toSet()))
						.build(),
				UserVO.builder().username("test3").password("test3@cometbid.com") // test3@cometbid.com
						.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("MANAGER"))
								.collect(Collectors.toSet()))
						.build() };

		UserVO userVO = UserVO.builder().username("test4").password("test4@cometbid.com") // test4@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("ADMIN")).collect(Collectors.toSet()))
				.build();

		userService.save(userVO).doOnSuccess(l -> log.info("User saved successfully...")).map(j -> {
			return tokenService.generateActivationToken(j.getId()).subscribe(c -> log.info("Activation token saved..."));
		}).subscribe(c -> log.info("User data saved..."));

		userVO = UserVO.builder().username("test5").password("test5@cometbid.com") // test5@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("SECRETARY"))
						.collect(Collectors.toSet()))
			.build();

		userService.save(userVO).doOnSuccess(l -> log.info("User saved successfully...")).map(j -> {
			return tokenService.generateActivationToken(j.getId()).subscribe(c -> log.info("Activation token saved..."));
		}).subscribe(c -> log.info("User data saved..."));

		userVO = UserVO.builder().username("test6").password("test6@cometbid.com") // test6@cometbid.com
				.roles(Role.getAllTypes().stream().filter(p -> p.equalsIgnoreCase("MANAGER"))
						.collect(Collectors.toSet()))
				.build();

		userService.save(userVO).doOnSuccess(l -> log.info("User saved successfully...")).map(j -> {
			return tokenService.generateActivationToken(j.getId()).subscribe(c -> log.info("Activation token saved..."));
		}).subscribe(c -> log.info("User data saved..."));

		List<UserVO> existingList = Arrays.asList(defaultUsers);

		userService.saveUsers(Flux.fromIterable(existingList)).subscribe();

		log.info("Done Initializing user data...");
	}

	private void initializeSuccessLoginData() {

		String ipAddr = "24.89.206.73";
		String userAgent = "testing/Agent";
		UsrLoginLoc loginLocHis = AuthHandler.getUserRelativeLocation(ipAddr, userAgent);

		SuccessLoginVO successLoginVo = SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test1@cometbid.com")
				.ipAddr(ipAddr).loginLocHis(loginLocHis).status(StatusType.VALID.name()).build();

		loginService.recordSuccessLogin(successLoginVo).subscribe(c -> log.info("Success login successfully saved..."));

		ipAddr = "116.134.151.61";
		loginLocHis = AuthHandler.getUserRelativeLocation(ipAddr, userAgent);
		successLoginVo = SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test2@cometbid.com").ipAddr(ipAddr)
				.loginLocHis(loginLocHis).status(StatusType.EXPIRED.name()).build();

		loginService.recordSuccessLogin(successLoginVo).subscribe(c -> log.info("Success login successfully saved..."));

		ipAddr = "149.116.147.168";
		loginLocHis = AuthHandler.getUserRelativeLocation(ipAddr, userAgent);
		successLoginVo = SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test3@cometbid.com").ipAddr(ipAddr)
				.loginLocHis(loginLocHis).status(StatusType.VALID.name()).build();

		loginService.recordSuccessLogin(successLoginVo).subscribe(c -> log.info("Success login successfully saved..."));

		ipAddr = "170.40.138.110";
		loginLocHis = AuthHandler.getUserRelativeLocation(ipAddr, userAgent);
		successLoginVo = SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test4@cometbid.com").ipAddr(ipAddr)
				.loginLocHis(loginLocHis).status(StatusType.EXPIRED.name()).build();

		loginService.recordSuccessLogin(successLoginVo).subscribe(c -> log.info("Success login successfully saved..."));

		log.info("Done Initializing succcess login data...");
	}

	private void initializeFailedLoginData() {

		String ipAddr = "24.89.206.73";
		loginService.incrementFailedLogins(ipAddr);

		ipAddr = "116.134.151.61";
		loginService.incrementFailedLogins(ipAddr);

		ipAddr = "149.116.147.168";
		loginService.incrementFailedLogins(ipAddr);

		ipAddr = "170.40.138.110";
		loginService.incrementFailedLogins(ipAddr);

		log.info("Done Initializing succcess login data...");
	}


}
