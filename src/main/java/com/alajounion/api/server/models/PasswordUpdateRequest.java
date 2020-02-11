/**
 * 
 */
package com.alajounion.api.server.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7151706463042473364L;

	@NotBlank
	private String userId;

	@NotBlank
	private String token;

	@NotBlank
	private String oldPassword;

	@NotBlank
	private String newPassword;

}
