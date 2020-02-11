/**
 * 
 */
package com.alajounion.api.secure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * @author Gbenga
 *
 */
@Configuration
@ComponentScan({ "com.alajounion.api.secure.services" })
public class MethodValidationConfig {
 
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}