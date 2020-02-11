/**
 * 
 */
package com.alajounion.api.secure.domain.mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.alajounion.api.secure.domain.FailedLogin;
import com.alajounion.api.secure.domain.SuccessLogin;
import com.alajounion.api.secure.domain.vo.SuccessLoginVO;
import com.alajounion.api.secure.embeddables.UsrLoginLoc;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.utils.DateUtil;
import com.cometbid.project.common.validators.GlobalProgrammaticValidator;

/**
 * @author Gbenga
 *
 */
public class SuccessLoginMapper {

	private SuccessLoginMapper() {
	}

	public static SuccessLogin expireStatus(SuccessLogin successLogin) {

		return SuccessLogin.builder().id(successLogin.getId()).ipAddr(successLogin.getIpAddr())
				.username(successLogin.getUsername()).logoutTime(successLogin.getLogoutTime())
				.loginTime(successLogin.getLoginTime())
				.logoutTime(DateUtil.NOW)
				.loginLocHis(successLogin.getLoginLocHis()).status(StatusType.EXPIRED.name()).build();
	}

	public static SuccessLogin create(SuccessLoginVO successLoginVo) {

		SuccessLogin login = SuccessLogin.builder().id(successLoginVo.getId()).ipAddr(successLoginVo.getIpAddr())
				.username(successLoginVo.getUsername())
				.loginTime(DateUtil.NOW)
				.status(StatusType.VALID.name()).build();

		UsrLoginLoc location = successLoginVo.getLoginLocHis();
		if (location != null) {
			login.addToLoginLocHis(successLoginVo.getLoginLocHis());
		}
		
		// Do Validation
		GlobalProgrammaticValidator.validateInput(login);

		return login;
	}

	public static SuccessLoginVO toViewObject(SuccessLogin successLogin) {

		return SuccessLoginVO.builder().id(successLogin.getId()).ipAddr(successLogin.getIpAddr())
				.username(successLogin.getUsername()).status(successLogin.getStatus()).build();
	}

	public static SuccessLogin toEntity(SuccessLoginVO successLoginVo) {

		SuccessLogin login = SuccessLogin.builder().id(successLoginVo.getId()).ipAddr(successLoginVo.getIpAddr())
				.username(successLoginVo.getUsername()).status(successLoginVo.getStatus()).build();

		UsrLoginLoc location = successLoginVo.getLoginLocHis();
		if (location != null) {
			login.addToLoginLocHis(successLoginVo.getLoginLocHis());
		}

		// Do Validation
		GlobalProgrammaticValidator.validateInput(login);

		return login;
	}

}
