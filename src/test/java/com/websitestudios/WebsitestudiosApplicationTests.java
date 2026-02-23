package com.websitestudios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Main application context test for Website Studios.
 * Verifies that the Spring Boot application context loads successfully.
 *
 * External services (Flyway, Redis, Mail) are excluded since
 * they are not available in the test environment.
 */
@SpringBootTest(properties = {
		"ws.security.encryption.aes-secret-key=TestEncryptionKey32CharactersXX!"
})
@EnableAutoConfiguration(exclude = {
		FlywayAutoConfiguration.class,
		RedisAutoConfiguration.class,
		RedisRepositoriesAutoConfiguration.class,
		MailSenderAutoConfiguration.class
})
class WebsitestudiosApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true, "Application context loaded successfully");
	}
}