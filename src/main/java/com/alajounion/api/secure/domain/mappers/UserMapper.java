/**
 * 
 */
package com.alajounion.api.secure.domain.mappers;

import java.util.UUID;
import java.util.stream.Collectors;

import com.alajounion.api.secure.domain.UserAuth;
import com.alajounion.api.secure.domain.vo.UserVO;
import com.cometbid.project.common.enums.Role;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.utils.DateUtil;
import com.cometbid.project.common.validators.GlobalProgrammaticValidator;
import com.cometbid.project.security.audit.Audit;

/**
 * @author Gbenga
 *
 */
public class UserMapper {

	private UserMapper() {
	}

	public static UserVO toViewObject(UserAuth user) {

		return UserVO.builder().id(user.getId()).username(user.getUsername()).password(user.getPassword())
				.roles(user.getAuthorities().stream().map(s -> s.getAuthority()).collect(Collectors.toSet()))
				.locked(StatusType.valueOf(user.getStatus()) == StatusType.LOCKED)
				.disabled(StatusType.valueOf(user.getStatus()) == StatusType.EXPIRED).build();
	}

	public static UserAuth create(UserVO userVo) {

		UserAuth user = UserAuth.builder().id(UUID.randomUUID().toString()).username(userVo.getUsername())
				.password(userVo.getPassword()).lastExpiration(DateUtil.NOW).roles(userVo.getRoles().stream()
						.map(s -> Role.fromString(s)).filter(p -> p != null).collect(Collectors.toSet()))
				.build();

		user.setStatus(StatusType.EXPIRED.name());

		Audit audit = new Audit();
		user = (UserAuth) audit.auditCreate(user);

		// Do Validation
		GlobalProgrammaticValidator.validateInput(user);

		return user;
	}

	public static UserAuth toEntity(UserVO userVo) {

		UserAuth user = UserAuth.builder().id(userVo.getId())
				.username(userVo.getUsername())
				.password(userVo.getPassword()).roles(userVo.getRoles().stream().map(s -> Role.fromString(s))
						.filter(p -> p != null).collect(Collectors.toSet()))
				.build();

		if (userVo.isDisabled()) {
			user.setStatus(StatusType.EXPIRED.name());
		} else if (userVo.isLocked()){
			user.setStatus(StatusType.LOCKED.name());
		} else {
			user.setStatus(StatusType.VALID.name());
		}

		Audit audit = new Audit();
		user = (UserAuth) audit.auditUpdate(user);

		// Do Validation
		GlobalProgrammaticValidator.validateInput(user);

		return user;
	}

}
