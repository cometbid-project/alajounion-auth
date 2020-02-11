package com.alajounion.api.server.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 
 * @author Gbenga
 *
 */
@Data
@NoArgsConstructor
public class AuthenticationRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6172203911260603601L;

	@NotBlank
	private String id;

	private String ipAddress;

	@NotBlank
	private String username;

	private String password;
	
	// private UsrLoginLoc location;

	public AuthenticationRequest(String username, String password) {
		this.setUsername(username);
		this.setPassword(password);
	}
}
