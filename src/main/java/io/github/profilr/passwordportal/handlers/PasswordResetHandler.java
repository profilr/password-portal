package io.github.profilr.passwordportal.handlers;

import io.github.profilr.passwordportal.IncorrectPasswordException;
import io.github.profilr.passwordportal.InvalidConfigurationException;

public interface PasswordResetHandler {
	
	public void init() throws InvalidConfigurationException;

	public void checkPassword(String username, String oldPassword) throws IncorrectPasswordException;
	
	public void resetPassword(String username, String oldPassword, String newPassword);
	
}