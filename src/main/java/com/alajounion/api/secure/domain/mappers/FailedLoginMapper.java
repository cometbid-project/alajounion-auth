/**
 * 
 */
package com.alajounion.api.secure.domain.mappers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.alajounion.api.secure.domain.FailedLogin;
import com.alajounion.api.secure.domain.vo.FailedLoginVO;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.utils.DateUtil;
import com.cometbid.project.common.validators.GlobalProgrammaticValidator;

/**
 * @author Gbenga
 *
 */
public class FailedLoginMapper {

	private FailedLoginMapper() {
	}

	public static FailedLogin expireStatus(FailedLoginVO failedLoginVo) {

		return FailedLogin.builder().id(failedLoginVo.getId()).ipAddr(failedLoginVo.getIpAddr())
				.loginAttempt(failedLoginVo.getLoginAttempt()).status(StatusType.EXPIRED.name()).build();
	}

	public static FailedLogin create(FailedLoginVO failedLoginVo) {

		FailedLogin failedLogin = FailedLogin.builder().id(UUID.randomUUID().toString())
				.ipAddr(failedLoginVo.getIpAddr()).loginAttempt(1).lastTime(DateUtil.NOW)
				.status(StatusType.VALID.name()).build();

		// Do Validation
		GlobalProgrammaticValidator.validateInput(failedLogin);

		return failedLogin;
	}

	public static FailedLoginVO toViewObject(FailedLogin failedLogin) {

		return FailedLoginVO.builder().id(failedLogin.getId()).ipAddr(failedLogin.getIpAddr())
				.loginAttempt(failedLogin.getLoginAttempt()).status(failedLogin.getStatus()).build();
	}

	public static FailedLogin toEntity(FailedLoginVO failedLoginVo) {

		FailedLogin failedLogin = FailedLogin.builder().id(failedLoginVo.getId()).ipAddr(failedLoginVo.getIpAddr())
				.lastTime(DateUtil.NOW).loginAttempt(failedLoginVo.getLoginAttempt()).status(failedLoginVo.getStatus())
				.build();

		// Do Validation
		GlobalProgrammaticValidator.validateInput(failedLogin);

		return failedLogin;
	}

}
