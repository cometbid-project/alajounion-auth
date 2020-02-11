package com.alajounion.api.server.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Gbenga
 *
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9136201574728380851L;

	private String username;
	private String token;

}
