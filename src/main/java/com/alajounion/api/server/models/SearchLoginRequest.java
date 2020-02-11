/**
 * 
 */
package com.alajounion.api.server.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
public class SearchLoginRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3896856342772458326L;

	private boolean success;

	@NotBlank(message = "{Login.startDate.notBlank}")
	private String startDate;

	@NotBlank(message = "{Login.endDate.notBlank}")
	private String endDate;

}
