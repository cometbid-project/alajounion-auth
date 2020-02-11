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
public class ExpireStatusUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3812598934962451724L;
	
	@NotBlank
	private boolean expire;
}
