package io.github.profilr.passwordportal;

public class IncorrectPasswordException extends Exception {

	private static final long serialVersionUID = 1L;

	public IncorrectPasswordException() {
		super("Incorrect Username or Password");
	}

}
