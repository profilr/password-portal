package io.github.profilr.passwordportal.handlerstest;

import io.github.profilr.passwordportal.IncorrectPasswordException;
import io.github.profilr.passwordportal.InvalidConfigurationException;
import io.github.profilr.passwordportal.handlers.PasswordResetHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestHandler2 implements PasswordResetHandler {

	private static boolean failOnce = true;

	@Override
	public void init() throws InvalidConfigurationException {
		log.info("init()");
	}

	@Override
	public void checkPassword(String username, String oldPassword) throws IncorrectPasswordException {
		log.info("checkPassword({}, {})", username, oldPassword);
		if (failOnce) {
			failOnce = false;
			throw new IncorrectPasswordException(new RuntimeException("Password Fail Test (try again)"));
		}
	}

	@Override
	public void resetPassword(String username, String oldPassword, String newPassword) {
		log.info("resetPassword({}, {}, {})", username, oldPassword, newPassword);
	}

}
