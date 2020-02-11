/**
 * 
 */
package com.alajounion.api.secure.domain.vo;

import javax.validation.constraints.NotBlank;
import com.cometbid.project.common.validators.qualifiers.IpAddress;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FailedLoginVO {

	private String id;

	@IpAddress
	@NotBlank(message = "{Login.ipAddr.notBlank}")
	private String ipAddr;

	private Integer loginAttempt;

	private String status; //

}
