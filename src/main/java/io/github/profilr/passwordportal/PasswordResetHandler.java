package io.github.profilr.passwordportal;

public interface PasswordResetHandler {

	public default void checkPassword(String username, String oldPassword) throws IncorrectPasswordException {}
	
	public void resetPassword(String username, String oldPassword, String newPassword);
	
}