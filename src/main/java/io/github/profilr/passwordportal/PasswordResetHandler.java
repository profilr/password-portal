package io.github.profilr.passwordportal;

import javax.servlet.ServletContext;

public interface PasswordResetHandler {
	
	public void init(ServletContext context) throws InvalidConfigurationException;

	public void checkPassword(String username, String oldPassword) throws IncorrectPasswordException;
	
	public void resetPassword(String username, String oldPassword, String newPassword);
	
}