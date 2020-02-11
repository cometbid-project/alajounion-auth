/**
 * 
 */
package com.alajounion.api.secure.config.async;

/**
 * @author Gbenga
 *
 */
import java.lang.reflect.Method;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(final Throwable throwable, final Method method, final Object... obj) {
		log.error("Exception occured with a message - {}", throwable.getMessage());
		log.error("Method name that threw exception - {}", method.getName());

		for (final Object param : obj) {
			log.info("Param - {}", param);
		}
	}

}