package com.websitestudios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 🌐 Website Studios API
 *
 * Main entry point for the Website Studios backend application.
 * A digital services platform offering Android, iOS,
 * and Web application development services.
 *
 * @author Website Studios Team
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class WebsitestudiosApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsitestudiosApplication.class, args);
	}
}