package io.github.profilr.passwordportal;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedeployPasswordResetHandler implements PasswordResetHandler {

	static final String DUMMY_PASSWORD = "{{{PASSWORD}}}";
	static final String HIBERNATE_PROPERTIES_PATH = "/WEB-INF/hibernate.properties";

	@Override
	public void init(ServletContext context) throws InvalidConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkPassword(String username, String oldPassword) throws IncorrectPasswordException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetPassword(String username, String oldPassword, String newPassword) {
		// TODO Auto-generated method stub
		
	}

}