/**
 * 
 */
package com.alajounion.api.secure.domain.mappers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.alajounion.api.secure.domain.FailedLogin;
import com.alajounion.api.secure.domain.UserActivation;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.utils.ActivationToken;
import com.cometbid.project.common.utils.DateUtil;
import com.cometbid.project.common.validators.GlobalProgrammaticValidator;

/**
 * @author Gbenga
 *
 */
public class UserActivationMapper {

	private UserActivationMapper() {
	}

	public static UserActivation create(ActivationToken activationToken) {

		UserActivation userAct = UserActivation.builder().id(UUID.randomUUID().toString())
				.userId(activationToken.getUserId()).token(activationToken.getToken()).status(StatusType.VALID.name())
				.creationDate(DateUtil.NOW).build();

		// Do Validation
		GlobalProgrammaticValidator.validateInput(userAct);
		
		return userAct;
	}

	public static ActivationToken toViewObject(UserActivation token) {

		return ActivationToken.builder().userId(token.getUserId()).token(token.getToken()).build();
	}

}
