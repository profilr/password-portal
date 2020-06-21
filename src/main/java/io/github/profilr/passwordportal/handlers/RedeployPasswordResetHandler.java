package io.github.profilr.passwordportal.handlers;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import io.github.profilr.passwordportal.InvalidConfigurationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedeployPasswordResetHandler implements PasswordResetHandler {

	static final String DUMMY_PASSWORD = "{{{PASSWORD}}}";
	static final String HIBERNATE_PROPERTIES_PATH = "/WEB-INF/hibernate.properties";
	
	private String hibernateProperties;
	private String destinationPath;

	@Override
	public void init(ServletContext context) throws InvalidConfigurationException {
		log.info("Reading in redeploy.properties");
		try {
			Properties properties = new Properties();
			properties.load(getClass().getResourceAsStream("redeploy.properties"));
			
		} catch (IOException e) {
			throw new InvalidConfigurationException("IOException in reading properties file. "
					+ "Please emsure that there is a valid hibernate.properties file in the WEB-INF directory.", e);
		}
	}

	@Override
	public void checkPassword(String username, String oldPassword) {}

	@Override
	public void resetPassword(String username, String oldPassword, String newPassword) {
		// TODO Auto-generated method stub
		
	}

}