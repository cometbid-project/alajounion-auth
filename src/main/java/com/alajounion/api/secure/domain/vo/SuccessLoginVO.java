/**
 * 
 */
package com.alajounion.api.secure.domain.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.alajounion.api.secure.embeddables.UsrLoginLoc;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.validators.qualifiers.IpAddress;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuccessLoginVO {

	private String id;

	@Size(min = 6, max = 330, message = "{Login.username.size}")
	@NotBlank(message = "{Login.username.notBlank}")
	private String username;

	@IpAddress(message = "{Login.ipAddr.invalid}")
	@NotBlank(message = "{Login.ipAddr.notBlank}")
	private String ipAddr;

	@Builder.Default
	private String status = StatusType.VALID.name();

	@JsonIgnore
	private UsrLoginLoc loginLocHis;

}
