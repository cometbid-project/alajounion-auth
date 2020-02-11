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
public class LockStatusUpdateRequest implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2455089826764647648L;
	
	@NotBlank
    private boolean lock;

}
