package com.alajounion.api.secure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan(basePackages = { "com.cometbid.project.security", "com.alajounion.api.secure",
		"com.cometbid.project.common" })
@SpringBootApplication
public class AuthApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

	public void run(String... strings) throws Exception {
		log.info("Initializing program data...");

	}

}
