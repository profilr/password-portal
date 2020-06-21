package io.github.profilr.passwordportal.handlers;

import javax.servlet.ServletContext;

import io.github.profilr.passwordportal.IncorrectPasswordException;
import io.github.profilr.passwordportal.InvalidConfigurationException;

public interface PasswordResetHandler {
	
	public void init(ServletContext context) throws InvalidConfigurationException;

	public void checkPassword(String username, String oldPassword) throws IncorrectPasswordException;
	
	public void resetPassword(String username, String oldPassword, String newPassword);
	
}