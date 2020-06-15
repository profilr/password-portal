package io.github.profilr.passwordportal;

public class InvalidConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidConfigurationException(String message) {
		super(message);
	}
	
	public InvalidConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
