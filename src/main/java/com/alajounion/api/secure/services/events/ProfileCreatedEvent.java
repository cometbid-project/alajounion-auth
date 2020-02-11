/**
 * 
 */
package com.alajounion.api.secure.services.events;

/**
 * @author Gbenga
 *
 */
import org.springframework.context.ApplicationEvent;
import com.alajounion.api.secure.domain.UserAuth;

public class ProfileCreatedEvent extends ApplicationEvent {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8101521104207812420L;

	public ProfileCreatedEvent(UserAuth source) {
        super(source);
    }
}
